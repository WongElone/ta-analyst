package traderalchemy.analyst.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.Const;
import traderalchemy.analyst.Global;
import traderalchemy.analyst.client.core.CoreApiClient;
import traderalchemy.analyst.client.strategy.StrategyApiClient;
import traderalchemy.analyst.repositoryreadonly.InductionVoRepositoryRo;
import traderalchemy.analyst.repositoryreadonly.InsightVoRepositoryRo;
import traderalchemy.analyst.service.InductionService;
import traderalchemy.analyst.service.TradebotDbService;
import traderalchemy.analyst.util.TimeRangeUtils;
import traderalchemy.analyst.vo.InductionOrInsightVo;
import traderalchemy.analyst.vo.InductionVo;
import traderalchemy.analyst.vo.InsightVo;
import traderalchemy.analyst.client.strategy.dto.InductionInstructionDto;
import traderalchemy.analyst.dao.InductionInstructionDao;
import traderalchemy.analyst.po.InducedPo;
import traderalchemy.analyst.po.InductionPo;
import traderalchemy.analyst.po.InductionInstructionPo;
import traderalchemy.analyst.bo.InductionResult;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InductionManager {

    private final CoreApiClient coreApiClient;
    private final StrategyApiClient strategyApiClient;
    private final InductionService inductionService;
    private final InsightVoRepositoryRo insightVoRepositoryRo;    
    private final InductionVoRepositoryRo inductionVoRepositoryRo;
    private final InductionInstructionDao inductionInstructionDao;
    private final TradebotDbService tradebotDbService;

    private final long inductionTimeoutSecs = 3600L;
    
    @Scheduled(cron = "${app.scheduler.induce-cron:0 0 0 * * ?}") // every day at 00:00:00 UTC
    public void induce() {
        log.info("Induction BEGIN");

        List<String> activeStrategies = getActiveStrategies();
        if (activeStrategies.isEmpty()) {
            log.info("Induction END, No active strategies");
            return;
        }
        log.info("Will induce for strategies: {}", activeStrategies);
        
        try {
            activeStrategies.stream().map(strategyClassName -> {
                return CompletableFuture
                    // 1. Get induction instructions from strategy API
                    .supplyAsync(() -> {
                        try {
                            return strategyApiClient.getInductionInstructions(strategyClassName);
                        } catch (Exception e) {
                            log.error("Strategy {} get induction instructions from strategy api failed, msg: {}", strategyClassName, e.getMessage(), e);
                            return null;
                        }
                    }, Global.executorService())
                    .exceptionally(e -> {
                        log.error("Strategy {} get induction instructions failed, msg: {}", strategyClassName, e.getMessage(), e);
                        return null;
                    })
                    // 2. Process each instruction
                    .thenAccept(instructions -> {
                        if (instructions == null || instructions.isEmpty()) {
                            return;
                        }
                        log.info("Strategy {} get induction instructions count: {}", strategyClassName, instructions.size());
                        instructions.stream().map(instruction -> CompletableFuture.runAsync(() -> {
                            final String md5 = instruction.genMd5();
                            // 2.1 Asynchronously ensure instruction is recorded in db
                            final var futureInstructionId = CompletableFuture.supplyAsync(() -> 
                                inductionInstructionDao.query(repo -> repo.findIdByMd5AndStrategyClassName(md5, strategyClassName))
                                    // 2.1.5 If instruction not exist in db, insert it
                                    .orElseGet(() -> inductionInstructionDao.txn(repo -> {
                                        return repo.save(buildInstructionPo(instruction, strategyClassName, md5)).getId();
                                    }))
                            , Global.executorService())
                            .exceptionally(e -> {
                                log.error("Strategy {} get instruction id failed for instruction {}, msg: {}", strategyClassName, instruction, e.getMessage(), e);
                                return null;
                            });
                            
                            // 2.2 Asynchronously perform induction
                            final var futureInductionResult = CompletableFuture.supplyAsync(() -> {
                                // 2.2.1 Find insights to induce
                                List<InsightVo> insights = insightVoRepositoryRo.findInsightsByStrategyAndTopicWithinTimeRangeOrderByCreateTimeAsc(
                                    strategyClassName, instruction.topic(), instruction.fromTime(), instruction.toTime());
                                if (insights == null || insights.size() < 2) { // nothing to induce, skip induction
                                    return null;
                                }
                                Map<Integer, InsightVo> insightMap = insights.stream().collect(Collectors.toMap(InsightVo::getId, Function.identity()));
                                List<InductionVo> previousInductions = List.of();
                                if (instruction.reuseOldInduction() && insights.size() > 5) {
                                    previousInductions = getPreviousInductions(strategyClassName, instruction, insights);
                                }
                                // 2.2.2 remove insights that are already covered by previous inductions
                                previousInductions.stream().forEach(inductionVo -> {
                                    insightMap.keySet().removeAll(inductionVo.getInsightIds());
                                });
                                List<InductionOrInsightVo> inductionOrInsightVos = new ArrayList<>(insightMap.values());
                                inductionOrInsightVos.addAll(previousInductions);
                                if (inductionOrInsightVos.size() < 2) { // nothing to induce, skip induction
                                    return null;
                                }
                                log.info("Strategy {} inducing insights with instruction: {}", strategyClassName, instruction);
                                return inductionService.induceThoughts(inductionOrInsightVos, instruction, true); // inductions were generated from the same instruction, they must be relevant
                            })
                            .exceptionally(e -> {
                                log.error("Strategy {} failed to induce insights with instruction: {}, msg: {}", strategyClassName, instruction, e.getMessage(), e);
                                return null;
                            });
                            
                            // 2.3 Wait for instruction id
                            final Integer instructionId = futureInstructionId.join();
                            if (instructionId == null) {
                                return;
                            }
                            
                            // 2.3 Wait for induction result
                            final InductionResult inductionResult = futureInductionResult.join();
                            if (inductionResult == null) {
                                log.info("Strategy {} induce gives nothing", strategyClassName);
                                return;
                            }
                            if (inductionResult.getConclusion() == null) {
                                log.warn("Strategy {} induce failed for instruction {} , msg: conclusion is null", strategyClassName, instruction);
                                return;
                            }
                            if (inductionResult.getReason() == null) {
                                log.warn("Strategy {} induce failed for instruction {} , msg: reason is null", strategyClassName, instruction);
                                return;
                            }
                            
                            // 2.4 Generate induced pos and induction pos
                            final List<InducedPo> inducedPos = new ArrayList<>();
                            final List<InductionPo> inductionPos = new ArrayList<>();
                            
                            inducedPos.add(InducedPo.builder()
                                .strategyClassName(strategyClassName)
                                .instructionId(instructionId)
                                .fromTime(instruction.fromTime())
                                .toTime(instruction.toTime())
                                .build());
                                
                            inductionPos.add(InductionPo.builder()
                                .strategyClassName(strategyClassName)
                                .topic(instruction.topic())
                                .fromTime(instruction.fromTime())
                                .toTime(instruction.toTime())
                                .insightIds(inductionResult.getInsightIds())
                                .inductionIds(inductionResult.getInductionIds())
                                .instructionId(instructionId)
                                .conclusion(inductionResult.getConclusion())
                                .reason(inductionResult.getReason())
                                .sourceTimeLowerBound(inductionResult.getSourceTimeLowerBound())
                                .sourceTimeUpperBound(inductionResult.getSourceTimeUpperBound())
                                .build());
                            
                            // 2.4 Insert induction records and insights with transaction
                            tradebotDbService.insertInduceRecordsAndInductions(inducedPos, inductionPos);
                        })
                        .exceptionally(e -> {
                            log.error("Strategy {} induction failed for instruction {} , msg: {}", strategyClassName, instruction, e.getMessage(), e);
                            return null;
                        })
                        ).forEach(CompletableFuture::join);
                    })
                    .exceptionally(e -> {
                        log.error("Strategy {} induction failed, msg: {}", strategyClassName, e.getMessage(), e);
                        return null;
                    });
            }).forEach(future -> {
                try {
                    future.get(inductionTimeoutSecs, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    log.error("timeout, msg: {}", e.getMessage(), e);
                } catch (ExecutionException e) {
                    log.error("execution failed, msg: {}", e.getMessage(), e);
                } catch (InterruptedException e) {
                    log.error("interrupted, msg: {}", e.getMessage(), e);
                } catch (CancellationException e) {
                    log.error("cancelled, msg: {}", e.getMessage(), e);
                } finally {
                    if (future.cancel(true)) {
                        log.info("Induction cancelled on timeout");
                    }
                }
            });
        } catch (Exception exception) {
            log.error("Induction failed, msg: {}", exception.getMessage(), exception);
        }
        
        log.info("Induction END");
    }

    /**
     * <p> get previous inductions that generated from the same instruction and over the time range covered by given insights </p>
     * <p> filter out the prev inductions whose insight time range (from time, to time) is covered by other prev inductions </p>
     * <p> such that inductions doesn't overlap with each other in the returned list </p>
     * @param sortedInsights insights sorted by create time ascending
     * @return
     */
    private List<InductionVo> getPreviousInductions(String strategyClassName, InductionInstructionDto instruction, List<InsightVo> sortedInsights) {
        final ZonedDateTime fromTime = sortedInsights.get(0).getCreateTime();
        final ZonedDateTime toTime = sortedInsights.get(sortedInsights.size() - 1).getCreateTime();
        String instructionMd5 = instruction.genMd5();
        List<InductionVo> prevInductions = inductionVoRepositoryRo.findInductionsByStrategyAndInstructionMd5AndInsightTimeRangeOrderByFromTimeAscToTimeDesc(
            strategyClassName, instructionMd5, fromTime, toTime);

        List<InductionVo> resultList = TimeRangeUtils.removeFullyCovered(prevInductions, true);
        return resultList;
    }

    private final List<String> getActiveStrategies() {
        try {
            return coreApiClient.getAllStrategiesStatus().entrySet().stream()
                .filter(status -> status.getValue() == Const.StrategyStatus.activated)
                .map(status -> status.getKey())
                .toList();
        } catch (Exception e) {
            log.error("Failed to get active strategies", e);
            return List.of();
        }
    }

    private static InductionInstructionPo buildInstructionPo(InductionInstructionDto dto, String strategyClassName, String md5) {
        return InductionInstructionPo.builder()
            .strategyClassName(strategyClassName)
            .topic(dto.topic())
            .predicate(dto.predicate())
            .analysis(dto.analysis())
            .answer(dto.answer().toString())
            .instructionMd5(md5)
            .build();
    }
}

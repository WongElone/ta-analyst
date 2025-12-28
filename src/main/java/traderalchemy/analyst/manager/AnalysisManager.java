package traderalchemy.analyst.manager;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.client.core.CoreApiClient;
import traderalchemy.analyst.client.strategy.StrategyApiClient;
import traderalchemy.analyst.client.strategy.dto.AnalysisInstructionDto;
import traderalchemy.analyst.dao.AnalysisInstructionDao;
import traderalchemy.analyst.po.AnalysedPo;
import traderalchemy.analyst.po.AnalysisInstructionPo;
import traderalchemy.analyst.po.ArticlePo;
import traderalchemy.analyst.po.FlashNewsPo;
import traderalchemy.analyst.po.InsightPo;
import traderalchemy.analyst.po.PoInterface;
import traderalchemy.analyst.po.SearchResultPo;
import traderalchemy.analyst.po.UserOpinionPo;
import traderalchemy.analyst.repositoryreadonly.ArticleRepositoryRo;
import traderalchemy.analyst.repositoryreadonly.FlashNewsRepositoryRo;
import traderalchemy.analyst.repositoryreadonly.UserOpinionRepositoryRo;
import traderalchemy.analyst.service.AnalysisService;
import traderalchemy.analyst.service.TradebotDbService;
import traderalchemy.analyst.Const;
import traderalchemy.analyst.Global;
import traderalchemy.analyst.Const.InsightCategory;
import traderalchemy.analyst.bo.AnalysisResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisManager {

    private final AnalysisService analysisService;
    private final ArticleRepositoryRo articleRepositoryRo;
    private final FlashNewsRepositoryRo flashNewsRepositoryRo;
    private final UserOpinionRepositoryRo userOpinionRepositoryRo;
    private final CoreApiClient coreApiClient;
    private final StrategyApiClient strategyApiClient;
    private final AnalysisInstructionDao analysisInstructionDao;
    private final TradebotDbService tradebotDbService;

    private final Map<InsightCategory, Long> categoryAnalyseTimeoutSecsMap = Map.of(
        InsightCategory.flashnews, 55L,
        InsightCategory.article, 600L,
        InsightCategory.research, 300L,
        InsightCategory.user, 300L
    );

    private final Map<InsightCategory, Integer> categoryAnalyseMaxCount = Map.of(
        InsightCategory.flashnews, 30,
        InsightCategory.article, 20,
        InsightCategory.user, 20
    );
    
    @Scheduled(cron = "${app.scheduler.analyse-flash-news-cron:0 * * * * ?}")
    public void analyseFlashNews() {
        analyse(
            InsightCategory.flashnews, 
            strategyClassName -> flashNewsRepositoryRo.findAllToBeAnalysed(strategyClassName, categoryAnalyseMaxCount.get(InsightCategory.flashnews)),
            analysisService::analyzeFlashNews, 
            FlashNewsPo::getPublishTime);
    }

    @Scheduled(cron = "${app.scheduler.analyse-articles-cron:0 15 * * * ?}")
    public void analyseArticles() {
        analyse(
            InsightCategory.article, 
            strategyClassName -> articleRepositoryRo.findAllToBeAnalysed(strategyClassName, categoryAnalyseMaxCount.get(InsightCategory.article)), 
            analysisService::analyzeArticles, 
            ArticlePo::getPublishTime);
    }

    public void research(String strategyClassName, List<SearchResultPo> searchResultPos, List<AnalysisInstructionDto> instructions) {
        analyse(
            InsightCategory.research, 
            strategy -> strategy.equals(strategyClassName) ? searchResultPos : List.of(), 
            analysisService::research,
            SearchResultPo::getCreateTime,
            strategy -> strategy.equals(strategyClassName) ? instructions : List.of());
    }

    @Scheduled(cron = "${app.scheduler.consult-user-cron:0 10 * * * ?}")
    public void consultUser() {
        analyse(
            InsightCategory.user, 
            strategyClassName -> userOpinionRepositoryRo.findAllToBeAnalysed(strategyClassName, categoryAnalyseMaxCount.get(InsightCategory.user)), 
            analysisService::consultUser, 
            UserOpinionPo::getCreateTime);
    }

    private <Po extends PoInterface> void analyse(
        Const.InsightCategory category, 
        Function<String, List<Po>> findAllToBeAnalysed, 
        BiFunction<List<Po>, AnalysisInstructionDto, Map<Integer, AnalysisResult>> analyseSources,
        Function<Po, ZonedDateTime> getSourceTime
    ) {
        analyse(category, findAllToBeAnalysed, analyseSources, getSourceTime, strategyClassName -> strategyApiClient.getAnalysisInstructions(strategyClassName, category));
    }

    private <Po extends PoInterface> void analyse(
        Const.InsightCategory category, 
        Function<String, List<Po>> findAllToBeAnalysed, 
        BiFunction<List<Po>, AnalysisInstructionDto, Map<Integer, AnalysisResult>> analyseSources,
        Function<Po, ZonedDateTime> getSourceTime,
        Function<String, List<AnalysisInstructionDto>> getInstructions
    ) {
        log.info("@{}@ Analyse BEGIN", category);
        List<String> activeStrategies = getActiveStrategies();
        if (activeStrategies.isEmpty()) {
            log.info("@{}@ Analyse END, No active strategies", category);
            return;
        }
        log.info("@{}@ Will analyse for strategies: {}", category, activeStrategies);
        try {
            activeStrategies.stream().map(strategyClassName -> {
                return CompletableFuture
                    // 1. query db get not yet analysed source
                    .supplyAsync(() -> {
                        final List<Po> pos = findAllToBeAnalysed.apply(strategyClassName);
                        log.info("@{}@{}@ get {} sources to be analysed", category, strategyClassName, pos.size());
                        return pos;
                    }, Global.executorService())
                    .exceptionally(e -> {
                        log.error("@{}@{}@ get sources to be analysed failed, msg: {}", category, strategyClassName, e.getMessage(), e);
                        return null;
                    })
                    // 2. analyse
                    .thenAccept(pos -> {
                        if (pos == null || pos.isEmpty()) {
                            return;
                        }
                        final List<AnalysisInstructionDto> instructions;
                        try {
                            instructions = getInstructions.apply(strategyClassName);
                        } catch (Exception e) {
                            log.error("@{}@{}@ get analysis instructions from strategy api failed, msg: {}", category, strategyClassName, e.getMessage(), e);
                            return;
                        }
                        log.info("@{}@{}@ get {} instructions", category, strategyClassName, instructions.size());
                        instructions.stream().map(instruction -> CompletableFuture.runAsync(() -> {
                            final String md5 = instruction.genMd5();
                            // 2.1 asyncrhonously ensure instruction is recorded in db
                            final var futureInstructionId = CompletableFuture.supplyAsync(() -> 
                                analysisInstructionDao.query(repo -> repo.findIdByMd5AndCategoryAndStrategyClassName(md5, category, strategyClassName))
                                    // 2.1.5 if instruction not exist in db, insert it
                                    .orElseGet(() -> analysisInstructionDao.txn(repo -> {
                                        return repo.save(buildInstructionPo(instruction, strategyClassName, md5, category)).getId();
                                    }))
                            , Global.executorService())
                            .exceptionally(e -> {
                                log.error("@{}@{}@ get instruction id failed for instruction {}, msg: {}", category, strategyClassName, instruction, e.getMessage(), e);
                                return null;
                            });
                            // 2.2 asyncrhonously analyse sources
                            final var futureAnalysisResultMap = CompletableFuture.supplyAsync(() -> {
                                return analyseSources.apply(pos, instruction);
                            }, Global.executorService())
                            .exceptionally(e -> {
                                log.error("@{}@{}@ analyse failed for instruction {}, msg: {}", category, strategyClassName, instruction, e.getMessage(), e);
                                return null;
                            });
                            // 2.3 wait for instruction id
                            final Integer instructionId = futureInstructionId.join();
                            if (instructionId == null) {
                                return;
                            }
                            // 2.4 wait for analysis result
                            final Map<Integer, AnalysisResult> analysisResultMap = futureAnalysisResultMap.join();
                            if (analysisResultMap == null || analysisResultMap.isEmpty()) {
                                return;
                            }
                            // 2.5 gen analysed pos and insight pos
                            final List<AnalysedPo> analysedPos = new ArrayList<>();
                            final List<InsightPo> insightPos = new ArrayList<>();
                            final Map<Integer, Po> posMap = pos.stream().collect(Collectors.toMap(Po::getId, Function.identity()));
                            analysisResultMap.forEach((sourceId, analysisResult) -> {
                                try {
                                    if (analysisResult.getConclusion() == null) {
                                        log.warn("@{}@{}@ analyse failed for instruction {}, msg: conclusion is null", category, strategyClassName, instruction);
                                        return;
                                    }
                                    if (analysisResult.getReason() == null) {
                                        log.warn("@{}@{}@ analyse failed for instruction {}, msg: reason is null", category, strategyClassName, instruction);
                                        return;
                                    }
                                    analysedPos.add(AnalysedPo.builder()
                                        .strategyClassName(strategyClassName)
                                        .category(category)
                                        .sourceId(sourceId)
                                        .instructionId(instructionId)
                                        .build());
                                    insightPos.add(InsightPo.builder()
                                        .strategyClassName(strategyClassName)
                                        .topic(instruction.topic())
                                        .category(category)
                                        .sourceId(sourceId)
                                        .instructionId(instructionId)
                                        .conclusion(analysisResult.getConclusion())
                                        .reason(analysisResult.getReason())
                                        .sourceTime(getSourceTime.apply(posMap.get(sourceId)))
                                        .build());
                                } catch (Exception e) {
                                    log.error("@{}@{}@ fail to build analysed pos and insight pos for source id {}, msg: {}", category, strategyClassName, sourceId, e.getMessage(), e);
                                    if (!analysedPos.isEmpty() && sourceId == analysedPos.getLast().getSourceId()) {
                                        analysedPos.removeLast();
                                    }
                                }
                            });
                            // 2.6 insert analysis record and insert insight with transaction
                            tradebotDbService.insertAnalysisRecordsAndInsights(analysedPos, insightPos);
                        })
                        .exceptionally(e -> {
                            log.error("@{}@{}@ analyse failed for instruction {}, msg: {}", category, strategyClassName, instruction, e.getMessage(), e);
                            return null;
                        })
                        ).forEach(CompletableFuture::join);
                    })
                    .exceptionally(e -> {
                        log.error("@{}@{}@ analyse failed on category {}, msg: {}", category, strategyClassName, e.getMessage(), e);
                        return null;
                    });
            }).forEach(future -> {
                try {
                    future.get(categoryAnalyseTimeoutSecsMap.get(category), TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    log.error("@{}@ analyse timeout, msg: {}", category, e.getMessage(), e);
                } catch (ExecutionException e) {
                    log.error("@{}@ analyse execution failed, msg: {}", category, e.getMessage(), e);
                } catch (InterruptedException e) {
                    log.error("@{}@ analyse interrupted, msg: {}", category, e.getMessage(), e);
                } catch (CancellationException e) {
                    log.error("@{}@ analyse cancelled, msg: {}", category, e.getMessage(), e);
                } finally {
                    future.cancel(true);
                }
            });
        } catch (Exception e) {
            log.error("@{}@ analyse failed, msg: {}", category, e.getMessage(), e);
        }
        log.info("@{}@ Analyse END", category);
    }


    private List<String> getActiveStrategies() {
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

    private static AnalysisInstructionPo buildInstructionPo(AnalysisInstructionDto dto, String strategyClassName, String md5, Const.InsightCategory category) {
        return AnalysisInstructionPo.builder()
            .strategyClassName(strategyClassName)
            .category(category)
            .topic(dto.topic())
            .predicate(dto.predicate())
            .analysis(dto.analysis())
            .answer(dto.answer())
            .instructionMd5(md5)
            .build();
    }
}

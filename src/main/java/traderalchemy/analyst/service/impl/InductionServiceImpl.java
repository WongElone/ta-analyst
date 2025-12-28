package traderalchemy.analyst.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import traderalchemy.analyst.Global;
import traderalchemy.analyst.bo.AnalysisResult;
import traderalchemy.analyst.bo.InductionResult;
import traderalchemy.analyst.client.openrouter.Openrouter;
import traderalchemy.analyst.client.strategy.dto.InductionInstructionDto;
import traderalchemy.analyst.config.PromptConfig;
import traderalchemy.analyst.service.InductionService;
import traderalchemy.analyst.util.ListUtils;
import traderalchemy.analyst.vo.InductionOrInsightVo;
import traderalchemy.analyst.vo.InductionVo;
import traderalchemy.analyst.vo.InsightVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InductionServiceImpl implements InductionService {

    private final Openrouter openrouter;
    
    @Value("${app.induction.filter-batch-size}")
    private int inductionFilterBatchSize;

    @Value("${app.induction.filter-include-example}")
    private boolean inductionFilterIncludeExample;

    @Value("${app.induction.batch-size:5}")
    private int inductionBatchSize;

    @Value("${app.induction.include-example}")
    private boolean inductionIncludeExample;

    @Value("${app.induction.batch-sleep-millis:1000}")
    private int inductionBatchSleepMillis;
    
    private static final ParameterizedTypeReference<Set<String>> FILTER_RESULT_TYPE = new ParameterizedTypeReference<Set<String>>() {};
    

    @Override
    public InductionResult induceThoughts(List<InductionOrInsightVo> voList, InductionInstructionDto inductionInstruction, boolean inductionsAreRelevant)
            throws RuntimeException {
        Set<String> relevantVoIds = new HashSet<>();
        Set<String> providedVoIds = voList.stream().map(InductionOrInsightVo::getVoId).collect(Collectors.toSet());
        if (Strings.isBlank(inductionInstruction.predicate())) {
            relevantVoIds.addAll(providedVoIds);
        } else {
            final List<InductionOrInsightVo> thoughtsToFilter = new ArrayList<>();
            voList.forEach(vo -> {
                switch (vo) {
                    case InsightVo insightVo -> thoughtsToFilter.add(insightVo);
                    case InductionVo inductionVo -> {
                        if (inductionsAreRelevant) {
                            relevantVoIds.add(inductionVo.getVoId());
                        } else {
                            thoughtsToFilter.add(inductionVo);
                        }
                    }
                }
            });
            // fast model filter insights with predicate
            ListUtils.batch(thoughtsToFilter, inductionFilterBatchSize).forEach(batch -> {
                // predicate not blank, need call LLM to filter thoughts
                try {
                    openrouter.request(Openrouter.Model.FAST, chatClient -> {
                            var halfDonePrompt = chatClient.prompt()
                                .system(PromptConfig.INSIGHT_FILTER_sys);
                            if (inductionFilterIncludeExample) {
                                halfDonePrompt = halfDonePrompt.messages(List.of(
                                    new UserMessage(PromptConfig.INSIGHT_FILTER_usr_eg),
                                    new AssistantMessage(PromptConfig.INSIGHT_FILTER_ans_eg)
                                ));
                            }
                            return halfDonePrompt.user(u -> {
                                    u.text(PromptConfig.INSIGHT_FILTER_usr);
                                    u.params(genFilterParams(inductionInstruction, batch));
                                })
                                .call()
                                .entity(FILTER_RESULT_TYPE);
                        })
                        .forEach(relevantVoIds::add);
                } catch (Exception e) {
                    log.error("filter insights failed, msg: {}", e.getMessage(), e);
                }
            });
        }
        log.debug("providedVoIds: {}", providedVoIds);
        log.debug("relevantVoIds: {}", relevantVoIds);
        if (relevantVoIds.isEmpty()) {
            return null;
        }
        relevantVoIds.retainAll(providedVoIds);
        final List<InductionOrInsightVo> sortedRelevantVoList =
            // filter out irrelevant
            voList.stream().filter(vo -> relevantVoIds.contains(vo.getVoId()))
            // sort by time for better organization for llm
            .sorted((vo1, vo2) -> vo1.compareTo(vo2))
            .collect(Collectors.toList());

        // split sortedRelevantVoList in batch
        
        List<InductionOrInsightVo> prevBatchList = new ArrayList<>(sortedRelevantVoList);
        String prettifiedAnswer = inductionInstruction.answer().toPrettyString();
        do {
            List<InductionOrInsightVo> currBatchList = new ArrayList<>();
            ListUtils.batch(prevBatchList, inductionBatchSize).forEach(batch -> {
                try {
                    AnalysisResult result = openrouter.request(Openrouter.Model.SLOW, chatClient -> {
                            var halfDonePrompt = chatClient.prompt()
                                .system(PromptConfig.INDUCTION_sys);
                            if (inductionIncludeExample) {
                                halfDonePrompt = halfDonePrompt.messages(List.of(
                                    new UserMessage(PromptConfig.INDUCTION_usr_eg),
                                    new AssistantMessage(PromptConfig.INDUCTION_ans_eg)
                                ));
                            }
                            return halfDonePrompt.user(u -> {
                                    u.text(PromptConfig.INDUCTION_usr);
                                    u.params(genInductionParams(inductionInstruction.analysis(), prettifiedAnswer, batch));
                                })
                                .call()
                                .entity(AnalysisResult.class);
                        });
                    InductionVo inductionOfBatch = InductionVo.builder()
                        .analysis(inductionInstruction.analysis())
                        .answer(prettifiedAnswer)
                        .conclusion(result.getConclusion())
                        .reason(result.getReason())
                        .fromTime(batch.stream().map(InductionOrInsightVo::getFromTime).min(Comparator.naturalOrder()).get())
                        .toTime(batch.stream().map(InductionOrInsightVo::getToTime).max(Comparator.naturalOrder()).get())
                        .sourceTimeLowerBound(batch.stream().map(InductionOrInsightVo::getSourceTimeLowerBound).min(Comparator.naturalOrder()).get())
                        .sourceTimeUpperBound(batch.stream().map(InductionOrInsightVo::getSourceTimeUpperBound).max(Comparator.naturalOrder()).get())
                        .build();
                    currBatchList.add(inductionOfBatch);
                } catch (Exception e) {
                    log.error("induce insights failed, msg: {}", e.getMessage(), e);
                }
                if (inductionBatchSleepMillis > 0) {
                    try {
                        Thread.sleep(inductionBatchSleepMillis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            });
            prevBatchList = currBatchList;
        } while (prevBatchList.size() > 1);
        // slow model induce insights
        final InductionVo result = (InductionVo) prevBatchList.get(0);
        Set<Integer> relevantInsightIds = new HashSet<>();
        Set<Integer> relevantInductionIds = new HashSet<>();
        sortedRelevantVoList.forEach(vo -> {
            switch (vo) {
                case InductionVo inductionVo -> {
                    relevantInductionIds.add(inductionVo.getId());
                    relevantInsightIds.addAll(inductionVo.getInsightIds());
                }
                case InsightVo insightVo -> {
                    relevantInsightIds.add(insightVo.getId());
                }
            }
        });
        return InductionResult.builder()
            .conclusion(result.getConclusion())
            .reason(result.getReason())
            .insightIds(relevantInsightIds)
            .inductionIds(relevantInductionIds)
            .sourceTimeLowerBound(sortedRelevantVoList.stream().map(InductionOrInsightVo::getSourceTimeLowerBound).min(Comparator.naturalOrder()).get())
            .sourceTimeUpperBound(sortedRelevantVoList.stream().map(InductionOrInsightVo::getSourceTimeUpperBound).max(Comparator.naturalOrder()).get())
            .build();
    }

    private Map<String, Object> genFilterParams(InductionInstructionDto inductionInstruction, List<InductionOrInsightVo> voList) {
        if (voList.stream().anyMatch(vo -> vo.getVoId() == null)) {
            throw new IllegalArgumentException("voList contains null voId");
        }
        return Map.of(
            "predicate", inductionInstruction.predicate(),
            "thoughts", Global.objectMapper().valueToTree(genThoughtListParams(voList)).toPrettyString() // in testing, spring ai can't read map, so convert to json string
        );
    }

    private Map<String, Object> genInductionParams(String analysis, String prettifiedAnswer, List<InductionOrInsightVo> voList) {
        return Map.of(
            "analysis", analysis,
            "answer", prettifiedAnswer,
            "thoughts", Global.objectMapper().valueToTree(genThoughtListParams(voList)).toPrettyString() // in testing, spring ai can't read map, so convert to json string
        );
    }

    private Map<String, Object> genThoughtListParams(List<InductionOrInsightVo> voList) {
        return voList.stream().map(vo -> {
            try {
                ObjectNode info = Global.objectMapper().createObjectNode();
                if (Strings.isNotBlank(vo.getVoId())) {
                    info.put("id", vo.getVoId());
                }
                info.put("type", vo instanceof InductionVo ? "induction" : "insight");
                info.put("analysis", vo.getAnalysis());
                info.put("answer", vo.getAnswer());
                info.put("conclusion", vo.getConclusion().toPrettyString());
                info.put("reason", vo.getReason());
                info.put("create_time(insight)", vo instanceof InductionVo ? null : Global.objectMapper().writeValueAsString(vo.getCreateTime()));
                info.put("from_time(induction)", Global.objectMapper().writeValueAsString(vo.getFromTime()));
                info.put("to_time(induction)", Global.objectMapper().writeValueAsString(vo.getToTime()));
                return info;
            } catch (JsonProcessingException e) {
                log.error("failed to serialize genInsightListParams, msg: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toMap(info -> info.get("id").asText(), Function.identity()));
    }
}

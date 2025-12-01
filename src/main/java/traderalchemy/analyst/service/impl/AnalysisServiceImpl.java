package traderalchemy.analyst.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.Global;
import traderalchemy.analyst.bo.AnalysisResult;
import traderalchemy.analyst.client.openrouter.Openrouter;
import traderalchemy.analyst.client.strategy.dto.AnalysisInstructionDto;
import traderalchemy.analyst.config.PromptConfig;
import traderalchemy.analyst.po.ArticlePo;
import traderalchemy.analyst.po.FlashNewsPo;
import traderalchemy.analyst.po.SearchResultPo;
import traderalchemy.analyst.po.UserOpinionPo;
import traderalchemy.analyst.service.AnalysisService;
import traderalchemy.analyst.util.ListUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {
    
    private final Openrouter openrouter;
    
    @Value("${app.flash-news.analysis-batch-size}")
    private int flashNewsAnalysisBatchSize;
    
    @Value("${app.flash-news.analysis-include-example}")
    private boolean flashNewsAnalysisIncludeExample;

    @Value("${app.article.filter-batch-size}")
    private int articleFilterBatchSize;

    @Value("${app.article.filter-include-example}")
    private boolean articleFilterIncludeExample;

    @Value("${app.article.analysis-include-example}")
    private boolean articleAnalysisIncludeExample;

    @Value("${app.research.include-example}")
    private boolean researchIncludeExample;

    @Value("${app.consult-user.include-example}")
    private boolean consultUserIncludeExample;

    private static final ParameterizedTypeReference<Map<Integer, AnalysisResult>> FLASH_NEWS_RESULT_TYPE = new ParameterizedTypeReference<Map<Integer, AnalysisResult>>() {};

    private static final ParameterizedTypeReference<Set<Integer>> FILTER_RESULT_TYPE = new ParameterizedTypeReference<Set<Integer>>() {};
    
    public Map<Integer, AnalysisResult> analyzeFlashNews(List<FlashNewsPo> flashNewsList, AnalysisInstructionDto analysisInstruction) throws RuntimeException {
        Map<Integer, AnalysisResult> result = new HashMap<>();
        ListUtils.batch(flashNewsList, flashNewsAnalysisBatchSize).forEach(batch -> {
            try {
                openrouter.request(Openrouter.Model.FAST, chatClient -> {
                        var halfDonePrompt = chatClient.prompt()
                            .system(PromptConfig.FLASH_NEWS_ANALYSIS_sys);
                        if (flashNewsAnalysisIncludeExample) {
                            halfDonePrompt = halfDonePrompt.messages(List.of(
                                new UserMessage(PromptConfig.FLASH_NEWS_ANALYSIS_usr_eg),
                                new AssistantMessage(PromptConfig.FLASH_NEWS_ANALYSIS_ans_eg)
                            ));
                        }
                        return halfDonePrompt.user(u -> {
                                u.text(PromptConfig.FLASH_NEWS_ANALYSIS_usr);
                                u.params(genFlashNewsParams(analysisInstruction, batch));
                            })
                            .call()
                            .entity(FLASH_NEWS_RESULT_TYPE);
                    })
                    .forEach((key, value) -> result.put(key, value));
            } catch (Exception e) {
                log.error("analyze flash news failed, msg: {}", e.getMessage(), e);
            }
        });
        log.debug("analyze flash news result: {}", result);
        return result;
    }

    private Map<String, Object> genFlashNewsParams(AnalysisInstructionDto analysisInstruction, List<FlashNewsPo> flashNewsList) {
        String sources = Global.objectMapper().valueToTree(genFlashNewsSourcesParam(flashNewsList)).toPrettyString();
        return Map.of(
            "predicate", analysisInstruction.predicate(),
            "analysis", analysisInstruction.analysis(),
            "answer", analysisInstruction.answer().toPrettyString(),
            "sources", sources
        );
    }

    private Map<String, Object> genFlashNewsSourcesParam(List<FlashNewsPo> flashNewsList) {
        Map<String, Object> map = new HashMap<>();
        for (FlashNewsPo flashNews : flashNewsList) {
            Map<String, Object> flashNewsNode = new HashMap<>();
            flashNewsNode.put("content", flashNews.getTitle() + (flashNews.getDescription() != null ? "\n\n" + flashNews.getDescription() : ""));
            flashNewsNode.put("publishTime", flashNews.getPublishTime());
            map.put(flashNews.getId().toString(), flashNewsNode);
        }
        return map;
    }

    public Map<Integer, AnalysisResult> analyzeArticles(List<ArticlePo> articlesList, AnalysisInstructionDto analysisInstruction) throws RuntimeException {
        Set<Integer> relevantArticleIds = new HashSet<>();
        Set<Integer> providedArticleIds = articlesList.stream().map(ArticlePo::getId).collect(Collectors.toSet());
        if (Strings.isBlank(analysisInstruction.predicate())) {
            relevantArticleIds.addAll(providedArticleIds);
        } else {
            // predicate not blank, need call LLM to filter articles
            ListUtils.batch(articlesList, articleFilterBatchSize).forEach(batch -> {
                try {
                    openrouter.request(Openrouter.Model.FAST, chatClient -> {
                            var halfDonePrompt = chatClient.prompt()
                                .system(PromptConfig.ARTICLES_FILTER_sys);
                            if (articleFilterIncludeExample) {
                                halfDonePrompt = halfDonePrompt.messages(List.of(
                                    new UserMessage(PromptConfig.ARTICLES_FILTER_usr_eg),
                                    new AssistantMessage(PromptConfig.ARTICLES_FILTER_ans_eg)
                                ));
                            }
                            return halfDonePrompt.user(u -> {
                                    u.text(PromptConfig.ARTICLES_FILTER_usr);
                                    u.params(genArticleFilterParams(analysisInstruction, batch));
                                })
                                .call()
                                .entity(FILTER_RESULT_TYPE);
                        })
                        .forEach(relevantArticleIds::add);
                } catch (Exception e) {
                    log.error("analyze articles failed, msg: {}", e.getMessage(), e);
                }
            });
        }
        log.debug("providedArticleIds: {}", providedArticleIds);
        log.debug("relevantArticleIds: {}", relevantArticleIds);
        if (relevantArticleIds.isEmpty()) {
            return new HashMap<>();
        }
        relevantArticleIds.retainAll(providedArticleIds);
        Map<Integer, AnalysisResult> analysisResultMap = new HashMap<>();
        for (ArticlePo article : articlesList) {
            try {
                if (!relevantArticleIds.contains(article.getId())) continue;
                AnalysisResult analysisResult = openrouter.request(Openrouter.Model.SLOW, chatClient -> {
                        var halfDonePrompt = chatClient.prompt()
                            .system(PromptConfig.ARTICLES_ANALYSIS_sys);
                        if (articleAnalysisIncludeExample) {
                            halfDonePrompt = halfDonePrompt.messages(List.of(
                                new UserMessage(PromptConfig.ARTICLES_ANALYSIS_usr_eg),
                                new AssistantMessage(PromptConfig.ARTICLES_ANALYSIS_ans_eg)
                            ));
                        }
                        return halfDonePrompt.user(u -> {
                                u.text(PromptConfig.ARTICLES_ANALYSIS_usr);
                                u.params(genArticleAnalysisParams(analysisInstruction, article));
                            })
                            .call()
                            .entity(AnalysisResult.class);
                    });
                analysisResultMap.put(article.getId(), analysisResult);
            } catch (Exception e) {
                log.error("analyze article failed, msg: {}", e.getMessage(), e);
            }
        }
        log.debug("analyze articles result: {}", analysisResultMap);
        return analysisResultMap;
    }

    private Map<String, Object> genArticleFilterParams(AnalysisInstructionDto analysisInstruction, List<ArticlePo> articleList) {
        return Map.of(
            "predicate", analysisInstruction.predicate(),
            "articleIdToTitleMap", genArticleIdToTitleMap(articleList).toPrettyString()
        );
    }

    private ObjectNode genArticleIdToTitleMap(List<ArticlePo> articleList) {
        ObjectNode objectNode = Global.objectMapper().createObjectNode();
        for (ArticlePo article : articleList) {
            objectNode.put(article.getId().toString(), article.getTitle());
        }
        return objectNode;
    }

    private Map<String, Object> genArticleAnalysisParams(AnalysisInstructionDto analysisInstruction, ArticlePo article) {
        return Map.of(
            "analysis", analysisInstruction.analysis(),
            "answer", analysisInstruction.answer().toPrettyString(),
            "publishTime", article.getPublishTime(),
            "title", article.getTitle(),
            "content", article.getContent()
        );
    }

    public Map<Integer, AnalysisResult> research(List<SearchResultPo> searchResultsList, AnalysisInstructionDto analysisInstruction) throws RuntimeException {
        Map<Integer, AnalysisResult> resultMap = new HashMap<>();
        for (SearchResultPo searchResult : searchResultsList) {
            try {
                AnalysisResult analysisResult = openrouter.request(Openrouter.Model.SLOW, chatClient -> {
                        var halfDonePrompt = chatClient.prompt()
                            .system(PromptConfig.RESEARCH_sys);
                        if (researchIncludeExample) {
                            halfDonePrompt = halfDonePrompt.messages(List.of(
                                new UserMessage(PromptConfig.RESEARCH_usr_eg),
                                new AssistantMessage(PromptConfig.RESEARCH_ans_eg)
                            ));
                        }
                        return halfDonePrompt.user(u -> {
                                u.text(PromptConfig.RESEARCH_usr);
                                u.params(genResearchParams(analysisInstruction, searchResult));
                            })
                            .call()
                            .entity(AnalysisResult.class);
                    });
                if (analysisResult.getConclusion() == null) {
                    log.info("research doeesn't match predicate, searchResult id: {}, predicate: {}, reason: {}", searchResult.getId(), analysisInstruction.predicate(), analysisResult.getReason());
                    continue;
                }
                resultMap.put(searchResult.getId(), analysisResult);
            } catch (Exception e) {
                log.error("research failed, searchResult: {}", searchResult, e);
            }
        }
        log.debug("research result: {}", resultMap);
        return resultMap;
    }

    private Map<String, Object> genResearchParams(AnalysisInstructionDto analysisInstruction, SearchResultPo searchResult) {
        return Map.of(
            "predicate", analysisInstruction.predicate(),
            "analysis", analysisInstruction.analysis(),
            "answer", analysisInstruction.answer().toPrettyString(),
            "searchTime", searchResult.getCreateTime(),
            "query", searchResult.getQuery(),
            "content", searchResult.getContent()
        );
    }

    public Map<Integer, AnalysisResult> consultUser(List<UserOpinionPo> userOpinionsList, AnalysisInstructionDto analysisInstruction) throws RuntimeException {
        Map<Integer, AnalysisResult> resultMap = new HashMap<>();
        for (UserOpinionPo userOpinion : userOpinionsList) {
            try {
                AnalysisResult analysisResult = openrouter.request(Openrouter.Model.SLOW, chatClient -> {
                        var halfDonePrompt = chatClient.prompt()
                            .system(PromptConfig.CONSULT_USER_sys);
                        if (consultUserIncludeExample) {
                            halfDonePrompt = halfDonePrompt.messages(List.of(
                                new UserMessage(PromptConfig.CONSULT_USER_usr_eg),
                                new AssistantMessage(PromptConfig.CONSULT_USER_ans_eg)
                            ));
                        }
                        return halfDonePrompt.user(u -> {
                                u.text(PromptConfig.CONSULT_USER_usr);
                                u.params(genConsultUserParams(analysisInstruction, userOpinion));
                            })
                            .call()
                            .entity(AnalysisResult.class);
                    });
                if (analysisResult.getConclusion() == null) {
                    log.info("consult user doeesn't match predicate, userOpinion id: {}, predicate: {}, reason: {}", userOpinion.getId(), analysisInstruction.predicate(), analysisResult.getReason());
                    continue;
                }
                resultMap.put(userOpinion.getId(), analysisResult);
            } catch (Exception e) {
                log.error("consult user failed, userOpinion: {}", userOpinion, e);
            }
        }
        log.debug("consult user result: {}", resultMap);
        return resultMap;
    }

    private Map<String, Object> genConsultUserParams(AnalysisInstructionDto analysisInstruction, UserOpinionPo userOpinion) {
        return Map.of(
            "predicate", analysisInstruction.predicate(),
            "analysis", analysisInstruction.analysis(),
            "answer", analysisInstruction.answer().toPrettyString(),
            "opinionTime", userOpinion.getCreateTime(),
            "opinion", userOpinion.getOpinion()
        );
    }
}

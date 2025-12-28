package traderalchemy.analyst.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.Const;
import traderalchemy.analyst.Global;
import traderalchemy.analyst.client.crawler.CrawlerApiClient;
import traderalchemy.analyst.client.crawler.dto.SearchResultDto;
import traderalchemy.analyst.client.openrouter.Openrouter;
import traderalchemy.analyst.config.PromptConfig;
import traderalchemy.analyst.dao.SearchResultDao;
import traderalchemy.analyst.dto.ResearchInstruction;
import traderalchemy.analyst.dto.SearchInstruction;
import traderalchemy.analyst.po.SearchResultPo;
import traderalchemy.analyst.service.SearchService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final Openrouter openrouter;
    private final CrawlerApiClient crawlerApiClient;
    private final SearchResultDao searchResultDao;
    
    public List<SearchResultPo> search(Const.SearchTool tool, ResearchInstruction researchInstruction) {
        final var searchInstruction = researchInstruction.getSearchInstruction();
        final String searchQuery = buildSearchQuery(searchInstruction);

        List<SearchResultDto> searchResultList;
        try {
            searchResultList = crawlerApiClient.doSearch(tool, searchQuery, searchInstruction.getFromTime(), searchInstruction.getToTime());
        } catch (Exception e) {
            log.error("Error searching, msg: {}", e.getMessage(), e);
            return List.of();
        }

        List<SearchResultPo> searchResultPoList = List.of();
        try {
            var toBeSaved = searchResultList.stream()
                .map(searchResult -> SearchResultPo.builder()
                    .strategyClassName(researchInstruction.getStrategyClassName())
                    .query(searchQuery)
                    .tool(tool)
                    .content(searchResult.getContent())
                    .contentMd5(Global.generateMD5(searchResult.getContent()))
                    .url(searchResult.getUrl())
                    .build()
            ).toList();
            searchResultPoList = searchResultDao.txn(repo -> {
                return repo.saveAll(toBeSaved);
            });
        } catch (Exception e) {
            log.error("Error saving search result, msg: {}", e.getMessage(), e);
        }
        return searchResultPoList;
    }

    private String buildSearchQuery(SearchInstruction searchInstruction) {
        final String searchQuery = openrouter.request(Openrouter.Model.FAST, chatClient -> {
            var halfDonePrompt = chatClient.prompt()
                .system(PromptConfig.BUILD_SEARCH_QUERY_sys);
            if (true) {
                halfDonePrompt = halfDonePrompt.messages(List.of(
                    new UserMessage(PromptConfig.BUILD_SEARCH_QUERY_usr_eg),
                    new AssistantMessage(PromptConfig.BUILD_SEARCH_QUERY_ans_eg)
                ));
            }
            return halfDonePrompt.user(u -> {
                    u.text(PromptConfig.BUILD_SEARCH_QUERY_usr);
                    u.params(Map.of(
                        "direction", searchInstruction.getDirection().name(),
                        "subject", searchInstruction.getSubject(),
                        "scopes", Global.objectMapper().valueToTree(searchInstruction.getScopes()).toString() // in testing, spring ai can't read map, so convert to json string
                    ));
                })
                .call()
                .entity(String.class);
        });
        log.info("Search query: \"{}\" built with instruction: {}", searchQuery, searchInstruction);
        return searchQuery;
    }
}

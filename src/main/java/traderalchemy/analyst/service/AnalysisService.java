package traderalchemy.analyst.service;

import traderalchemy.analyst.po.FlashNewsPo;
import traderalchemy.analyst.po.ArticlePo;
import traderalchemy.analyst.po.SearchResultPo;
import traderalchemy.analyst.po.UserOpinionPo;
import traderalchemy.analyst.bo.AnalysisResult;
import traderalchemy.analyst.client.strategy.dto.AnalysisInstructionDto;

import java.util.List;
import java.util.Map;

/**
 * Interface for analyzing different types of sources (flash news, articles, search results) 
 * based on AnalysisInstruction.
 */
public interface AnalysisService {
    
    /**
     * Analyzes a list of flash news based on a single AnalysisInstruction.
     * 
     * @param flashNewsList List of flash news to analyze
     * @param analysisInstruction The analysis instruction to apply
     * @return Map of flash news ID to AnalysisResult containing conclusion and reason
     */
    Map<Integer, AnalysisResult> analyzeFlashNews(List<FlashNewsPo> flashNewsList, AnalysisInstructionDto analysisInstruction) throws RuntimeException;
    
    /**
     * Analyzes a list of articles based on a single AnalysisInstruction.
     * 
     * @param articlesList List of articles to analyze
     * @param analysisInstruction The analysis instruction to apply
     * @return Map of article ID to AnalysisResult containing conclusion and reason
     */
    Map<Integer, AnalysisResult> analyzeArticles(List<ArticlePo> articlesList, AnalysisInstructionDto analysisInstruction) throws RuntimeException;
    
    /**
     * Analyzes a list of search results based on a single AnalysisInstruction.
     * 
     * @param searchResultsList List of search results to analyze
     * @param analysisInstruction The analysis instruction to apply
     * @return Map of search result ID to AnalysisResult containing conclusion and reason
     */
    Map<Integer, AnalysisResult> research(List<SearchResultPo> searchResultsList, AnalysisInstructionDto analysisInstruction) throws RuntimeException;

    /**
     * Analyzes a list of user opinions based on a single AnalysisInstruction.
     * 
     * @param userOpinionsList List of user opinions to analyze
     * @param analysisInstruction The analysis instruction to apply
     * @return Map of user opinion ID to AnalysisResult containing conclusion and reason
     */
    Map<Integer, AnalysisResult> consultUser(List<UserOpinionPo> userOpinionsList, AnalysisInstructionDto analysisInstruction) throws RuntimeException;

}

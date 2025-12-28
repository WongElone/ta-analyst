package traderalchemy.analyst.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.Const;
import traderalchemy.analyst.Global;
import traderalchemy.analyst.client.strategy.dto.AnalysisInstructionDto;
import traderalchemy.analyst.dto.ResearchInstruction;
import traderalchemy.analyst.dto.ResponseWrapper;
import traderalchemy.analyst.manager.AnalysisManager;
import traderalchemy.analyst.po.SearchResultPo;
import traderalchemy.analyst.service.SearchService;

@Slf4j
@RestController
@RequestMapping("/research")
@RequiredArgsConstructor
public class ResearchController {
    
    private final AnalysisManager analysisManager;
    private final SearchService searchService;

    @Value("${app.research.search-tool}")
    private String searchTool;

    @PostMapping
    public ResponseWrapper<Void> newResearch(@RequestBody ResearchInstruction researchInstruction) {
        final Const.SearchTool tool;
        try {
            tool = Const.SearchTool.valueOf(searchTool);
        } catch (IllegalArgumentException e) {
            return ResponseWrapper.failure(ResponseWrapper.CODE_INVALID_ARGUMENT, ResponseWrapper.MSG_INVALID_ARGUMENT);
        }
        // TODO: validate search instruction

        Global.executorService().submit(() -> {
            List<SearchResultPo> searchResultPoList = searchService.search(tool, researchInstruction);
            if (searchResultPoList.isEmpty()) {
                return;
            }

            analysisManager.research(researchInstruction.getStrategyClassName(), searchResultPoList, 
                researchInstruction.getAnalysisInstructions().stream()
                    .map(instruction -> new AnalysisInstructionDto(
                        instruction.getTopic(),
                        instruction.getPredicate(),
                        instruction.getAnalysis(),
                        instruction.getAnswer()
                    ))
                    .toList());
        });
        return ResponseWrapper.success(null);
    }
}

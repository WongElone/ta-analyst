package traderalchemy.analyst.service;

import java.util.List;

import traderalchemy.analyst.Const;
import traderalchemy.analyst.dto.ResearchInstruction;
import traderalchemy.analyst.po.SearchResultPo;

public interface SearchService {

    /**
     * Search for the given research instruction with given tool, return empty list if failed.
     * 
     * @param tool
     * @param researchInstruction
     * @return
     */
    List<SearchResultPo> search(Const.SearchTool tool, ResearchInstruction researchInstruction);
}

package traderalchemy.analyst.service;

import traderalchemy.analyst.client.strategy.dto.InductionInstructionDto;
import traderalchemy.analyst.vo.InductionOrInsightVo;

import java.util.List;

import traderalchemy.analyst.bo.InductionResult;

public interface InductionService {
    
    /**
     * Induces collection of insights or previous inductions to induction based on InductionInstruction.
     * 
     * @param insightsOrInductions The collection of insights or inductions to induce
     * @param inductionInstruction The induction instruction to apply
     * @param inductionsAreRelevant Whether inductions are relevant to the instruction
     * @return InductionResult containing conclusion and reason
     */
    InductionResult induceThoughts(List<InductionOrInsightVo> insightsOrInductions, InductionInstructionDto inductionInstruction, boolean inductionsAreRelevant) throws RuntimeException;

}

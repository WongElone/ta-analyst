package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.AnalysisInstructionRepository;

@Component
public class AnalysisInstructionDao extends Dao<AnalysisInstructionRepository> {
    
    public AnalysisInstructionDao(AnalysisInstructionRepository repo) {
        super(repo);
    }
}

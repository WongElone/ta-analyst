package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.InductionInstructionRepository;

@Component
public class InductionInstructionDao extends Dao<InductionInstructionRepository> {
    
    public InductionInstructionDao(InductionInstructionRepository repo) {
        super(repo);
    }
}

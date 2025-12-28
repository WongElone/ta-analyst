package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.InductionRepository;

@Component
public class InductionDao extends Dao<InductionRepository> {
    
    public InductionDao(InductionRepository repo) {
        super(repo);
    }
}

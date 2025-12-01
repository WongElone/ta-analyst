package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.AnalysedRepository;

@Component
public class AnalysedDao extends Dao<AnalysedRepository> {
    
    public AnalysedDao(AnalysedRepository repo) {
        super(repo);
    }
}

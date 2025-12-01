package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.InsightRepository;

@Component
public class InsightDao extends Dao<InsightRepository> {
    
    public InsightDao(InsightRepository repo) {
        super(repo);
    }
}

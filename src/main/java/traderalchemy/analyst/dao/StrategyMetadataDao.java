package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.StrategyMetadataRepository;

@Component
public class StrategyMetadataDao extends Dao<StrategyMetadataRepository> {
    
    public StrategyMetadataDao(StrategyMetadataRepository repo) {
        super(repo);
    }
}

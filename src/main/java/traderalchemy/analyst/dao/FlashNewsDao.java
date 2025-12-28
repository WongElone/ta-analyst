package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.FlashNewsRepository;

@Component
public class FlashNewsDao extends Dao<FlashNewsRepository> {
    
    public FlashNewsDao(FlashNewsRepository repo) {
        super(repo);
    }
}

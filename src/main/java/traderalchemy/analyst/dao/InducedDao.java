package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.InducedRepository;

@Component
public class InducedDao extends Dao<InducedRepository> {
    
    public InducedDao(InducedRepository repo) {
        super(repo);
    }
}

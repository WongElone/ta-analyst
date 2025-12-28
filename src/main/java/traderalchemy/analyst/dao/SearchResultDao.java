package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.SearchResultRepository;

@Component
public class SearchResultDao extends Dao<SearchResultRepository> {
    
    public SearchResultDao(SearchResultRepository repo) {
        super(repo);
    }
}

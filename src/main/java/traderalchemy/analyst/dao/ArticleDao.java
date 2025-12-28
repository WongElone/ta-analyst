package traderalchemy.analyst.dao;

import org.springframework.stereotype.Component;

import traderalchemy.analyst.repository.ArticleRepository;

@Component
public class ArticleDao extends Dao<ArticleRepository> {
    
    public ArticleDao(ArticleRepository repo) {
        super(repo);
    }
}

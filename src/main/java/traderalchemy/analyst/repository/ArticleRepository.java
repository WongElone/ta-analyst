package traderalchemy.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.ArticlePo;

@Repository
public interface ArticleRepository extends JpaRepository<ArticlePo, Integer> {
}

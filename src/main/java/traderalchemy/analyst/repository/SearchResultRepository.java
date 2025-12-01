package traderalchemy.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.SearchResultPo;

@Repository
public interface SearchResultRepository extends JpaRepository<SearchResultPo, Integer> {
}

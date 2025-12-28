package traderalchemy.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.AnalysedPo;

@Repository
public interface AnalysedRepository extends JpaRepository<AnalysedPo, Integer> {
}

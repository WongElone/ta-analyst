package traderalchemy.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.InductionPo;

@Repository
public interface InductionRepository extends JpaRepository<InductionPo, Integer> {
}

package traderalchemy.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.InducedPo;

@Repository
public interface InducedRepository extends JpaRepository<InducedPo, Integer> {
}

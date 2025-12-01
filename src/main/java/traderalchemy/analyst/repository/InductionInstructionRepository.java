package traderalchemy.analyst.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.InductionInstructionPo;

@Repository
public interface InductionInstructionRepository extends JpaRepository<InductionInstructionPo, Integer> {

    @Query("SELECT i.id FROM InductionInstructionPo i WHERE i.instructionMd5 = ?1 AND i.strategyClassName = ?2 ORDER BY i.createTime DESC LIMIT 1")
    Optional<Integer> findIdByMd5AndStrategyClassName(String instructionMd5, String strategyClassName);
}

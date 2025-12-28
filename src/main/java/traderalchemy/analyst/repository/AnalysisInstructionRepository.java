package traderalchemy.analyst.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.po.AnalysisInstructionPo;
import traderalchemy.analyst.Const;

@Repository
public interface AnalysisInstructionRepository extends JpaRepository<AnalysisInstructionPo, Integer> {

    @Query("select a.id from AnalysisInstructionPo a where a.instructionMd5 = :md5 and a.category = :category and a.strategyClassName = :strategyClassName ORDER BY a.createTime DESC LIMIT 1")
    Optional<Integer> findIdByMd5AndCategoryAndStrategyClassName(String md5, Const.InsightCategory category, String strategyClassName);
}

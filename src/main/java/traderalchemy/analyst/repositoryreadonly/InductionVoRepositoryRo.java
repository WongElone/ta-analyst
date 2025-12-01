package traderalchemy.analyst.repositoryreadonly;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.vo.InductionVo;

@Repository
public interface InductionVoRepositoryRo extends JpaRepository<InductionVo, Integer> {
    
    @Query("SELECT i FROM InductionVo i WHERE i.deleted = false AND i.strategyClassName = ?1 AND i.instructionMd5 = ?2 AND i.fromTime >= ?3 AND i.toTime <= ?4 ORDER BY i.fromTime ASC, i.toTime DESC")
    List<InductionVo> findInductionsByStrategyAndInstructionMd5AndInsightTimeRangeOrderByFromTimeAscToTimeDesc(String strategyClassName, String instructionMd5, ZonedDateTime fromTime, ZonedDateTime toTime);
}

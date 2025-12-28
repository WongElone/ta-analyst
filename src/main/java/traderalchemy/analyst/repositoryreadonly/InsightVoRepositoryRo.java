package traderalchemy.analyst.repositoryreadonly;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.vo.InsightVo;

@Repository
public interface InsightVoRepositoryRo extends JpaRepository<InsightVo, Integer> {
    
    @Query("SELECT i FROM InsightVo i WHERE i.deleted = false AND i.strategyClassName = ?1 AND i.topic = ?2 AND i.createTime BETWEEN ?3 AND ?4 ORDER BY i.createTime ASC")
    List<InsightVo> findInsightsByStrategyAndTopicWithinTimeRangeOrderByCreateTimeAsc(String strategyClassName, String topic, ZonedDateTime fromTime, ZonedDateTime toTime);
}

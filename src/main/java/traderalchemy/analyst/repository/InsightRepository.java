package traderalchemy.analyst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import traderalchemy.analyst.Const;
import traderalchemy.analyst.po.InsightPo;

@Repository
public interface InsightRepository extends JpaRepository<InsightPo, Integer> {
    
    Boolean existsByCategoryAndSourceId(Const.InsightCategory category, Integer sourceId);

}

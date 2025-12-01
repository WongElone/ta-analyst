package traderalchemy.analyst;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import traderalchemy.analyst.vo.InductionOrInsightVo;
import traderalchemy.analyst.vo.InductionVo;
import traderalchemy.analyst.vo.InsightVo;

public class CompareTest {
    
    @Test
    public void testMinMaxZonedDateTime() {
        InsightVo insightVo1 = new InsightVo();
        insightVo1.setSourceTime(ZonedDateTime.now());
        InsightVo insightVo2 = new InsightVo();
        insightVo2.setSourceTime(ZonedDateTime.now().minusHours(1));
        InsightVo insightVo3 = new InsightVo();
        insightVo3.setSourceTime(ZonedDateTime.now().plusHours(1));
        InductionVo inductionVo1 = InductionVo.builder().build();
        inductionVo1.setSourceTimeLowerBound(ZonedDateTime.now());
        inductionVo1.setSourceTimeUpperBound(ZonedDateTime.now().plusHours(2));
        List<InductionOrInsightVo> voList = List.of(
            insightVo1,
            insightVo2,
            inductionVo1,
            insightVo3
        );
        ZonedDateTime min = voList.stream().map(InductionOrInsightVo::getSourceTimeLowerBound).min(Comparator.naturalOrder()).get();
        ZonedDateTime max = voList.stream().map(InductionOrInsightVo::getSourceTimeUpperBound).max(Comparator.naturalOrder()).get();
        System.out.println(min);
        System.out.println(max);
    }
}

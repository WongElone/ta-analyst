package traderalchemy.analyst;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.util.TimeRangeUtils;
import traderalchemy.analyst.vo.InductionVo;

@Slf4j
public class InductionTests {

    @Test
    public void testMap() {
        Map<Integer, Integer> map1 = new HashMap<>();
        map1.put(1, 1);
        map1.put(2, 2);
        map1.put(3, 3);
        
        map1.keySet().removeAll(List.of(1, 2));
        log.info("map1: {}", map1);
    }

    @Test
    public void testRemoveFullyCovered() {
        // Test case 1: Empty list
        assertTrue(TimeRangeUtils.removeFullyCovered(List.of()).isEmpty(),
                "Empty list should return empty list");

        // Test case 2: Single induction
        InductionVo single = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z");
        List<InductionVo> result = TimeRangeUtils.removeFullyCovered(List.of(single));
        assertEquals(1, result.size(), "Single induction should be returned");
        assertEquals(single, result.get(0), "Single induction should be unchanged");

        // Test case 3: Two inductions, no overlap
        InductionVo ind1 = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z");
        InductionVo ind2 = createInduction(2, "2023-01-03T00:00:00Z", "2023-01-04T00:00:00Z");
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(ind1, ind2));
        assertEquals(2, result.size(), "Both non-overlapping inductions should be kept");
        assertTrue(result.contains(ind1), "First induction should be kept");
        assertTrue(result.contains(ind2), "Second induction should be kept");

        // Test case 4: Two inductions, second completely covered by first
        InductionVo wide = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-05T00:00:00Z");
        InductionVo narrow = createInduction(2, "2023-01-02T00:00:00Z", "2023-01-03T00:00:00Z");
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(wide, narrow));
        assertEquals(1, result.size(), "Only the wider induction should be kept");
        assertEquals(wide, result.get(0), "Wider induction should be kept");

        // Test case 5: Two inductions, partial overlap (not completely covered)
        InductionVo early = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-03T00:00:00Z");
        InductionVo late = createInduction(2, "2023-01-02T00:00:00Z", "2023-01-04T00:00:00Z");
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(early, late));
        assertEquals(2, result.size(), "Both partially overlapping inductions should be kept");
        assertTrue(result.contains(early), "Early induction should be kept");
        assertTrue(result.contains(late), "Late induction should be kept");

        // Test case 6: Multiple inductions with complex overlaps
        InductionVo a = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-10T00:00:00Z"); // Wide range
        InductionVo b = createInduction(2, "2023-01-02T00:00:00Z", "2023-01-03T00:00:00Z"); // Covered by A
        InductionVo c = createInduction(3, "2023-01-04T00:00:00Z", "2023-01-06T00:00:00Z"); // Covered by A
        InductionVo d = createInduction(4, "2023-01-11T00:00:00Z", "2023-01-15T00:00:00Z"); // New range
        InductionVo e = createInduction(5, "2023-01-12T00:00:00Z", "2023-01-13T00:00:00Z"); // Covered by D
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(a, b, c, d, e));
        assertEquals(2, result.size(), "Only A and D should be kept");
        assertTrue(result.contains(a), "Wide range A should be kept");
        assertTrue(result.contains(d), "New range D should be kept");

        // Test case 7: Same fromTime, different toTime (prefer wider range)
        InductionVo shortRange = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z");
        InductionVo longRange = createInduction(2, "2023-01-01T00:00:00Z", "2023-01-05T00:00:00Z");
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(shortRange, longRange));
        assertEquals(1, result.size(), "Only the longer range should be kept");
        assertEquals(longRange, result.get(0), "Longer range should be kept");

        // Test case 8: Same toTime, different fromTime
        InductionVo earlyStart = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-05T00:00:00Z");
        InductionVo lateStart = createInduction(2, "2023-01-03T00:00:00Z", "2023-01-05T00:00:00Z");
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(earlyStart, lateStart));
        assertEquals(1, result.size(), "Only the earlier starting induction should be kept");
        assertEquals(earlyStart, result.get(0), "Earlier starting induction should be kept");

        // Test case 9: Already sorted in optimal order
        InductionVo first = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-03T00:00:00Z");
        InductionVo second = createInduction(2, "2023-01-04T00:00:00Z", "2023-01-06T00:00:00Z");
        InductionVo third = createInduction(3, "2023-01-07T00:00:00Z", "2023-01-09T00:00:00Z");
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(first, second, third));
        assertEquals(3, result.size(), "All non-overlapping inductions should be kept");
        assertEquals(Arrays.asList(first, second, third), result, "Order should be preserved");

        // Test case 10: Reverse sorted order
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(third, second, first));
        assertEquals(3, result.size(), "All non-overlapping inductions should be kept");
        assertEquals(Arrays.asList(first, second, third), result, "Should be sorted correctly");

        // Test case 11: Duplicate time ranges
        InductionVo dup1 = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-05T00:00:00Z");
        InductionVo dup2 = createInduction(2, "2023-01-01T00:00:00Z", "2023-01-05T00:00:00Z");
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(dup1, dup2));
        assertEquals(1, result.size(), "Duplicate ranges should result in one kept induction");

        // Test case 12: Complex overlapping scenario with mixed order
        InductionVo complex1 = createInduction(1, "2023-01-05T00:00:00Z", "2023-01-10T00:00:00Z");
        InductionVo complex2 = createInduction(2, "2023-01-01T00:00:00Z", "2023-01-06T00:00:00Z");
        InductionVo complex3 = createInduction(3, "2023-01-02T00:00:00Z", "2023-01-04T00:00:00Z"); // Covered
        InductionVo complex4 = createInduction(4, "2023-01-11T00:00:00Z", "2023-01-15T00:00:00Z");
        InductionVo complex5 = createInduction(5, "2023-01-12T00:00:00Z", "2023-01-14T00:00:00Z"); // Covered
        InductionVo complex6 = createInduction(6, "2023-01-08T00:00:00Z", "2023-01-12T00:00:00Z");
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(complex1, complex2, complex3, complex4, complex5, complex6));
        assertEquals(4, result.size(), "Complex scenario should keep 4 inductions");
        // Should keep: complex2 (earliest, widest), complex6 (extends beyond), complex4 (new range)
        assertTrue(result.contains(complex1), "Complex1 should be kept");
        assertTrue(result.contains(complex2), "Complex2 should be kept");
        assertTrue(result.contains(complex6), "Complex6 should be kept");
        assertTrue(result.contains(complex4), "Complex4 should be kept");
        result.forEach(r -> log.info("from: {}, to: {}", r.getFromTime(), r.getToTime()));

        // Test case 13: Adjacent time ranges (touching but not overlapping)
        InductionVo adj1 = createInduction(1, "2023-01-01T00:00:00Z", "2023-01-02T00:00:00Z");
        InductionVo adj2 = createInduction(2, "2023-01-02T00:00:00Z", "2023-01-03T00:00:00Z");
        result = TimeRangeUtils.removeFullyCovered(Arrays.asList(adj1, adj2));
        assertEquals(2, result.size(), "Adjacent ranges should both be kept");
        assertTrue(result.contains(adj1), "First adjacent induction should be kept");
        assertTrue(result.contains(adj2), "Second adjacent induction should be kept");
    }

    private InductionVo createInduction(int id, String fromTimeStr, String toTimeStr) {
        InductionVo vo = InductionVo.builder().build();
        vo.setId(id);
        vo.setFromTime(ZonedDateTime.parse(fromTimeStr));
        vo.setToTime(ZonedDateTime.parse(toTimeStr));
        // Set other required fields to minimal values for testing
        vo.setStrategyClassName("test");
        vo.setTopic("test");
        vo.setInsightIds(Set.of());
        vo.setInductionIds(Set.of());
        vo.setInstructionId(1);
        vo.setConclusion(null); // Can be null for this test
        vo.setReason("test");
        vo.setSourceTimeLowerBound(ZonedDateTime.parse(fromTimeStr));
        vo.setSourceTimeUpperBound(ZonedDateTime.parse(toTimeStr));
        vo.setCreateTime(ZonedDateTime.now());
        vo.setPredicate("test");
        vo.setAnalysis("test");
        vo.setAnswer("test");
        vo.setInstructionMd5("test");
        return vo;
    }
}

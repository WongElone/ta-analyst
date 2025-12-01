package traderalchemy.analyst.util;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimeRangeUtils {
    
    public interface TimeRange {
        ZonedDateTime getFromTime();
        ZonedDateTime getToTime();
    }

    public static <T extends TimeRange> List<T> removeFullyCovered(List<T> ranges) {
        return removeFullyCovered(ranges, false);
    }

    /**
     * filter out the prev inductions whose insight time range (from time, to time) is covered by other prev inductions
     * @param ranges
     * @param isSorted
     * @return filtered prev inductions sorted in ascending order by from time
     */
    public static <T extends TimeRange> List<T> removeFullyCovered(List<T> ranges, boolean isSorted) {
        // Filter out inductions whose time range is covered by other inductions
        // Algorithm: O(n log n) time complexity
        // 1. Sort by fromTime ascending, then by toTime descending (to prioritize wider ranges)
        // 2. Keep track of max toTime seen so far
        // 3. If current range's toTime <= maxToTime, it's covered by a previous range, skip it
        
        if (ranges.isEmpty()) {
            return List.of();
        }
        
        // Sort: primary by fromTime ascending, secondary by toTime descending
        List<T> sorted = new ArrayList<>(ranges);
        if (!isSorted) {
            sorted.sort((a, b) -> {
                int fromCompare = a.getFromTime().compareTo(b.getFromTime());
                if (fromCompare != 0) {
                    return fromCompare;
                }
                // If fromTime is equal, prefer the one with later toTime (wider range)
                return b.getToTime().compareTo(a.getToTime());
            });
        }
        
        ZonedDateTime maxToTime = null;
        List<T> resultList = new ArrayList<>();
        
        for (T range : sorted) {
            // If this is the first range or its toTime extends beyond maxToTime, keep it
            if (maxToTime == null || range.getToTime().isAfter(maxToTime)) {
                resultList.add(range);
                maxToTime = range.getToTime();
            }
            // Otherwise, this range is covered by a previous range, skip it
        }        
        return resultList;
    }
}

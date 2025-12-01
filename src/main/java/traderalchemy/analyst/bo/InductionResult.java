package traderalchemy.analyst.bo;

import java.util.Set;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InductionResult {
    private ObjectNode conclusion;
    private String reason;
    Set<Integer> insightIds;
    Set<Integer> inductionIds;
    ZonedDateTime sourceTimeLowerBound;
    ZonedDateTime sourceTimeUpperBound;
}

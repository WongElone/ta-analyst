package traderalchemy.analyst.client.strategy.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import traderalchemy.analyst.Global;

public record InductionInstructionDto(
    String topic,
    /** insight create time >= from_time */
    @JsonProperty("from_time")
    ZonedDateTime fromTime,
    /** insight create time <= to_time */
    @JsonProperty("to_time")
    ZonedDateTime toTime,
    String predicate,
    String analysis,
    ObjectNode answer,
    @JsonProperty("reuse_old_induction")
    boolean reuseOldInduction
) {

    public String genMd5() {
        StringBuilder sb = new StringBuilder()
            .append(topic)
            .append(predicate)
            .append(analysis)
            .append(answer);
        return Global.generateMD5(sb.toString());
    }
}

package traderalchemy.analyst.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisInstruction {
    @JsonProperty("topic")
    String topic;
    @JsonProperty("predicate")
    String predicate;
    @JsonProperty("analysis")
    String analysis;
    @JsonProperty("answer")
    ObjectNode answer;
}

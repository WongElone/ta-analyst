package traderalchemy.analyst.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResearchInstruction {

    @JsonProperty("strategy_class_name")
    private String strategyClassName;

    @JsonProperty("search_instruction")
    private SearchInstruction searchInstruction;

    @JsonProperty("analysis_instructions")
    private List<AnalysisInstruction> analysisInstructions;
}

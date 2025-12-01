package traderalchemy.analyst.bo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Result of analyzing a source (flash news, article, or search result) based on AnalysisInstruction.
 * Contains conclusion and reason fields that correspond to Insight's conclusion and reason.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    /**
     * The conclusion of the analysis as a JSON object.
     * Corresponds to Insight's conclusion field.
     */
    private ObjectNode conclusion;
    
    /**
     * The reason for the analysis conclusion as text.
     * Corresponds to Insight's reason field.
     */
    private String reason;
}

package traderalchemy.analyst.client.strategy.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;

import traderalchemy.analyst.Global;

public record AnalysisInstructionDto(
    String topic,
    String predicate,
    String analysis,
    ObjectNode answer
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

package traderalchemy.analyst.client.strategy.dto;

import java.util.List;

public record GetAnalysisInstructionsResponseDto(
    boolean success,
    String errorMessage,
    List<AnalysisInstructionDto> result
) {
}

package traderalchemy.analyst.client.strategy.dto;

import java.util.List;

public record GetInductionInstructionsResponseDto(
    boolean success,
    String errorMessage,
    List<InductionInstructionDto> result
) {
}

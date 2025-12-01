package traderalchemy.analyst.client.strategy.dto;

import traderalchemy.analyst.Const;

public record GetAnalysisInstructionsRequestDto(
    String strategy_class_name,
    Const.InsightCategory category
) {
}

package traderalchemy.analyst.client.core.dto;

import java.util.Map;

import traderalchemy.analyst.Const;

public record GetAllStrategiesStatusResponseDto(Map<String, Const.StrategyStatus> result) {
}

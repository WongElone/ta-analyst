package traderalchemy.analyst.client.core;

import retrofit2.Call;
import retrofit2.http.GET;

import traderalchemy.analyst.client.core.dto.GetAllStrategiesStatusResponseDto;

public interface CoreApi {
    @GET("/strategy/status/all")
    Call<GetAllStrategiesStatusResponseDto> getAllStrategiesStatus();
}

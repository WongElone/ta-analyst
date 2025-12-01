package traderalchemy.analyst.client.strategy;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import traderalchemy.analyst.client.strategy.dto.GetAnalysisInstructionsRequestDto;
import traderalchemy.analyst.client.strategy.dto.GetAnalysisInstructionsResponseDto;
import traderalchemy.analyst.client.strategy.dto.GetInductionInstructionsRequestDto;
import traderalchemy.analyst.client.strategy.dto.GetInductionInstructionsResponseDto;

public interface StrategyApi {
    @POST("/get-analysis-instructions")
    Call<GetAnalysisInstructionsResponseDto> getAnalysisInstructions(@Body GetAnalysisInstructionsRequestDto request);
    
    @POST("/get-induction-instructions")
    Call<GetInductionInstructionsResponseDto> getInductionInstructions(@Body GetInductionInstructionsRequestDto request);
}

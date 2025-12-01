package traderalchemy.analyst.client.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import traderalchemy.analyst.Global;
import traderalchemy.analyst.Const;
import traderalchemy.analyst.client.strategy.dto.GetAnalysisInstructionsRequestDto;
import traderalchemy.analyst.client.strategy.dto.GetAnalysisInstructionsResponseDto;
import traderalchemy.analyst.client.strategy.dto.GetInductionInstructionsRequestDto;
import traderalchemy.analyst.client.strategy.dto.GetInductionInstructionsResponseDto;
import traderalchemy.analyst.client.strategy.dto.AnalysisInstructionDto;
import traderalchemy.analyst.client.strategy.dto.InductionInstructionDto;

@Slf4j
@Component
public class StrategyApiClient {
    
    private final StrategyApi strategyApi;

    public StrategyApiClient(
        @Value("${strategy.host}") String host,
        @Value("${strategy.port}") Integer port
    ) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://" + host + ":" + port)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(Global.objectMapper()))
            .build();

        this.strategyApi = retrofit.create(StrategyApi.class);
    }

    public List<AnalysisInstructionDto> getAnalysisInstructions(String strategyClassName, Const.InsightCategory category) {
        GetAnalysisInstructionsRequestDto request = new GetAnalysisInstructionsRequestDto(strategyClassName, category);
        GetAnalysisInstructionsResponseDto response = executeSync(strategyApi.getAnalysisInstructions(request));
        
        if (!response.success()) {
            throw new RuntimeException("Failed to get analysis instructions: " + response.errorMessage());
        }
        
        return response.result();
    }

    public List<InductionInstructionDto> getInductionInstructions(String strategyClassName) {
        GetInductionInstructionsRequestDto request = new GetInductionInstructionsRequestDto(strategyClassName);
        GetInductionInstructionsResponseDto response = executeSync(strategyApi.getInductionInstructions(request));
        
        if (!response.success()) {
            throw new RuntimeException("Failed to get induction instructions: " + response.errorMessage());
        }
        
        return response.result();
    }

    private <T> T executeSync(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                log.error("Strategy API call failed: response status code={}, message={}, errorBody={}", response.code(), response.message(), errorBody);
                throw new RuntimeException("Strategy API call failed: " + response.code() + " " + response.message() + " - " + errorBody);
            }
        } catch (Exception e) {
            log.error("Strategy API call failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

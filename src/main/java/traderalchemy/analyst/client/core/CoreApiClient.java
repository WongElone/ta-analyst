package traderalchemy.analyst.client.core;

import org.springframework.stereotype.Component;

import java.util.Map;
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
import traderalchemy.analyst.client.core.dto.GetAllStrategiesStatusResponseDto;

@Slf4j
@Component
public class CoreApiClient {
    
    private final CoreApi coreApi;

    public CoreApiClient(
        @Value("${core.host}") String host,
        @Value("${core.port}") Integer port
    ) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://" + host + ":" + port)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(Global.objectMapper()))
            .build();

        this.coreApi = retrofit.create(CoreApi.class);
    }

    public Map<String, Const.StrategyStatus> getAllStrategiesStatus() throws Exception {
        GetAllStrategiesStatusResponseDto response = executeSync(coreApi.getAllStrategiesStatus());
        return response.result();
    }

    private <T> T executeSync(Call<T> call) throws Exception {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                log.error("Core API call failed: response status code={}, message={}, errorBody={}", response.code(), response.message(), errorBody);
                throw new RuntimeException("Core API call failed: " + response.code() + " " + response.message() + " - " + errorBody);
            }
        } catch (Exception e) {
            log.error("Core API call failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}

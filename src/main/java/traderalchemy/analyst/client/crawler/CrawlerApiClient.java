package traderalchemy.analyst.client.crawler;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import traderalchemy.analyst.Global;
import traderalchemy.analyst.client.crawler.dto.CrawlerApiResponse;
import traderalchemy.analyst.client.crawler.dto.DoSearchRequestDto;
import traderalchemy.analyst.client.crawler.dto.SearchResultDto;
import traderalchemy.analyst.Const;
/**
 * Client for calling crawler API endpoints
 */
@Slf4j
@Component
public class CrawlerApiClient {
    
    private final CrawlerApi crawlerApi;

    public CrawlerApiClient(
        @Value("${crawler.host}") String host,
        @Value("${crawler.port}") Integer port
    ) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(125, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://" + host + ":" + port)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(Global.objectMapper()))
            .build();

        this.crawlerApi = retrofit.create(CrawlerApi.class);
    }

    /**
     * Call crawler to perform a search
     * 
     * @param tool search tool
     * @param query Built search query string
     * @param fromTime (Optional) Start time for search range
     * @param toTime (Optional) End time for search range
     * @return List of search results
     * @throws Exception if the API call fails
     */
    public List<SearchResultDto> doSearch(Const.SearchTool tool, String query, ZonedDateTime fromTime, ZonedDateTime toTime) throws Exception {
        DoSearchRequestDto request = DoSearchRequestDto.builder()
            .tool(tool)
            .query(query)
            .fromTime(fromTime)
            .toTime(toTime)
            .build();
        return executeSync(crawlerApi.doSearch(request));
    }

    private <T> T executeSync(Call<CrawlerApiResponse<T>> call) throws Exception {
        try {
            Response<CrawlerApiResponse<T>> response = call.execute();
            if (response.isSuccessful()) {
                CrawlerApiResponse<T> crawlerApiResponse = response.body();
                if (crawlerApiResponse.getSuccess() == null || !crawlerApiResponse.getSuccess()) {
                    throw new RuntimeException("Crawler API call failed: " + crawlerApiResponse.getErrorMessage());
                }
                return crawlerApiResponse.getResult();
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                log.error("Crawler API call failed: response status code={}, message={}, errorBody={}", response.code(), response.message(), errorBody);
                throw new RuntimeException("Crawler API call failed: " + response.code() + " " + response.message() + " - " + errorBody);
            }
        } catch (Exception e) {
            log.error("Crawler API call failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}

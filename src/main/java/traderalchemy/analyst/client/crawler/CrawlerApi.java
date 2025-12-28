package traderalchemy.analyst.client.crawler;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import traderalchemy.analyst.client.crawler.dto.CrawlerApiResponse;
import traderalchemy.analyst.client.crawler.dto.DoSearchRequestDto;
import traderalchemy.analyst.client.crawler.dto.SearchResultDto;

/**
 * Crawler API interface for Retrofit
 */
public interface CrawlerApi {
    /**
     * Call crawler to perform a search
     * 
     * @param request Search request with query and time range
     * @return Search response with list of results wrapped in CrawlerApiResponse
     */
    @POST("/search")
    Call<CrawlerApiResponse<List<SearchResultDto>>> doSearch(@Body DoSearchRequestDto request);
}

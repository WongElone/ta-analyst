package traderalchemy.analyst.client.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response wrapper for crawler API endpoints
 * All crawler API responses follow this format: success, errorMessage, result
 * 
 * @param <T> The type of the result data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerApiResponse<T> {
    /**
     * Indicates if the API call was successful
     */
    private Boolean success;
    
    /**
     * Error message if the call failed
     */
    private String errorMessage;
    
    /**
     * The result data
     */
    private T result;
}

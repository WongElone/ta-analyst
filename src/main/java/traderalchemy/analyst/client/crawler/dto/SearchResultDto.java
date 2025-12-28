package traderalchemy.analyst.client.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Search result DTO returned from crawler
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    /**
     * Search result content
     */
    private String content;
    
    /**
     * URL of the search result (optional)
     */
    private String url;
}

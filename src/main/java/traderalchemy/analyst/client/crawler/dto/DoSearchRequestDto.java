package traderalchemy.analyst.client.crawler.dto;

import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;
import traderalchemy.analyst.Const;

/**
 * Request DTO for crawler search endpoint
 */
@Data
@Builder
public class DoSearchRequestDto {

    private Const.SearchTool tool;
    /**
     * Built search query string
     */
    private String query;
    
    /**
     * Start time for search range
     */
    private ZonedDateTime fromTime;
    
    /**
     * End time for search range
     */
    private ZonedDateTime toTime;
}

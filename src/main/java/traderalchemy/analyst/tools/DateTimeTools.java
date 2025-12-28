package traderalchemy.analyst.tools;

import java.time.ZonedDateTime;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.Global;

@Slf4j
@Component
public class DateTimeTools {
    
    @Tool(description = "Get the current zoned datetime string with ISO 8601 format in the user's timezone")
    public String getCurrentUserDateTime() {
        try {
            return Global.objectMapper().writeValueAsString(ZonedDateTime.now());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize current system datetime, msg: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

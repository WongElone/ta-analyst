package traderalchemy.analyst;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicTest {
    
    @Test
    public void testDateTimeString() throws JsonProcessingException {
        log.info(ZonedDateTime.now().toString());
        log.info(Global.objectMapper().writeValueAsString(ZonedDateTime.now()));
    }
}

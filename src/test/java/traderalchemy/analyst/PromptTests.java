package traderalchemy.analyst;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.config.PromptConfig;

@Slf4j
public class PromptTests {
    
    @Test
    public void testLoadPrompt() {
        log.info(PromptConfig.FLASH_NEWS_ANALYSIS_sys);
    }
}

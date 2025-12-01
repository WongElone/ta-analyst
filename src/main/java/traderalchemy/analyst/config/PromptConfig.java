package traderalchemy.analyst.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PromptConfig {

    // ---- flash news analysis ----

    public static final String FLASH_NEWS_ANALYSIS_sys;
    
    public static final String FLASH_NEWS_ANALYSIS_usr;

    public static final String FLASH_NEWS_ANALYSIS_usr_eg;

    public static final String FLASH_NEWS_ANALYSIS_ans_eg;

    // ---- articles analysis ----

    public static final String ARTICLES_FILTER_sys;

    public static final String ARTICLES_FILTER_usr;

    public static final String ARTICLES_FILTER_usr_eg;

    public static final String ARTICLES_FILTER_ans_eg;

    public static final String ARTICLES_ANALYSIS_sys;

    public static final String ARTICLES_ANALYSIS_usr;

    public static final String ARTICLES_ANALYSIS_usr_eg;

    public static final String ARTICLES_ANALYSIS_ans_eg;

    // ---- research ----

    public static final String RESEARCH_sys;

    public static final String RESEARCH_usr;

    public static final String RESEARCH_usr_eg;

    public static final String RESEARCH_ans_eg;

    // ---- consult user ----

    public static final String CONSULT_USER_sys;

    public static final String CONSULT_USER_usr;

    public static final String CONSULT_USER_usr_eg;

    public static final String CONSULT_USER_ans_eg;
    
    // ---- induction ----

    public static final String INSIGHT_FILTER_sys;

    public static final String INSIGHT_FILTER_usr;

    public static final String INSIGHT_FILTER_usr_eg;

    public static final String INSIGHT_FILTER_ans_eg;

    public static final String INDUCTION_sys;

    public static final String INDUCTION_usr;

    public static final String INDUCTION_usr_eg;

    public static final String INDUCTION_ans_eg;

    // ---- build search query ----

    public static final String BUILD_SEARCH_QUERY_sys;

    public static final String BUILD_SEARCH_QUERY_usr;

    public static final String BUILD_SEARCH_QUERY_usr_eg;

    public static final String BUILD_SEARCH_QUERY_ans_eg;

    static {
        try {
            FLASH_NEWS_ANALYSIS_sys = loadPromptFromResource("prompt/FLASH_NEWS_ANALYSIS_sys.md");
            FLASH_NEWS_ANALYSIS_usr = loadPromptFromResource("prompt/FLASH_NEWS_ANALYSIS_usr.md");
            FLASH_NEWS_ANALYSIS_usr_eg = loadPromptFromResource("prompt/FLASH_NEWS_ANALYSIS_usr_eg.md");
            FLASH_NEWS_ANALYSIS_ans_eg = loadPromptFromResource("prompt/FLASH_NEWS_ANALYSIS_ans_eg.md");
            
            ARTICLES_FILTER_sys = loadPromptFromResource("prompt/ARTICLES_FILTER_sys.md");
            ARTICLES_FILTER_usr = loadPromptFromResource("prompt/ARTICLES_FILTER_usr.md");
            ARTICLES_FILTER_usr_eg = loadPromptFromResource("prompt/ARTICLES_FILTER_usr_eg.md");
            ARTICLES_FILTER_ans_eg = loadPromptFromResource("prompt/ARTICLES_FILTER_ans_eg.md");
            ARTICLES_ANALYSIS_sys = loadPromptFromResource("prompt/ARTICLES_ANALYSIS_sys.md");
            ARTICLES_ANALYSIS_usr = loadPromptFromResource("prompt/ARTICLES_ANALYSIS_usr.md");
            ARTICLES_ANALYSIS_usr_eg = loadPromptFromResource("prompt/ARTICLES_ANALYSIS_usr_eg.md");
            ARTICLES_ANALYSIS_ans_eg = loadPromptFromResource("prompt/ARTICLES_ANALYSIS_ans_eg.md");
            
            RESEARCH_sys = loadPromptFromResource("prompt/RESEARCH_sys.md");
            RESEARCH_usr = loadPromptFromResource("prompt/RESEARCH_usr.md");
            RESEARCH_usr_eg = loadPromptFromResource("prompt/RESEARCH_usr_eg.md");
            RESEARCH_ans_eg = loadPromptFromResource("prompt/RESEARCH_ans_eg.md");
            
            CONSULT_USER_sys = loadPromptFromResource("prompt/CONSULT_USER_sys.md");
            CONSULT_USER_usr = loadPromptFromResource("prompt/CONSULT_USER_usr.md");
            CONSULT_USER_usr_eg = loadPromptFromResource("prompt/CONSULT_USER_usr_eg.md");
            CONSULT_USER_ans_eg = loadPromptFromResource("prompt/CONSULT_USER_ans_eg.md");
            
            INSIGHT_FILTER_sys = loadPromptFromResource("prompt/INSIGHT_FILTER_sys.md");
            INSIGHT_FILTER_usr = loadPromptFromResource("prompt/INSIGHT_FILTER_usr.md");
            INSIGHT_FILTER_usr_eg = loadPromptFromResource("prompt/INSIGHT_FILTER_usr_eg.md");
            INSIGHT_FILTER_ans_eg = loadPromptFromResource("prompt/INSIGHT_FILTER_ans_eg.md");
            INDUCTION_sys = loadPromptFromResource("prompt/INDUCTION_sys.md");
            INDUCTION_usr = loadPromptFromResource("prompt/INDUCTION_usr.md");
            INDUCTION_usr_eg = loadPromptFromResource("prompt/INDUCTION_usr_eg.md");
            INDUCTION_ans_eg = loadPromptFromResource("prompt/INDUCTION_ans_eg.md");

            BUILD_SEARCH_QUERY_sys = loadPromptFromResource("prompt/BUILD_SEARCH_QUERY_sys.md");
            BUILD_SEARCH_QUERY_usr = loadPromptFromResource("prompt/BUILD_SEARCH_QUERY_usr.md");
            BUILD_SEARCH_QUERY_usr_eg = loadPromptFromResource("prompt/BUILD_SEARCH_QUERY_usr_eg.md");
            BUILD_SEARCH_QUERY_ans_eg = loadPromptFromResource("prompt/BUILD_SEARCH_QUERY_ans_eg.md");

        } catch (IOException e) {
            log.error("Failed to load prompt files, msg: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load prompt files", e);
        }
    }

    private static String loadPromptFromResource(String resourcePath) throws IOException {
        try (InputStream inputStream = PromptConfig.class.getClassLoader().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            return content.toString().trim();
        }
    }
}

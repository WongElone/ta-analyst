package traderalchemy.analyst.config;

import java.time.Duration;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;


@Configuration
@RequiredArgsConstructor
public class AiModelConfig {
    
    private final Secrets secrets;

    @Value("${spring.ai.openai.http.client.connect-timeout-secs}")
    private int openAiConnectTimeoutSecs;
    @Value("${spring.ai.openai.http.client.read-timeout-secs}")
    private int openAiReadTimeoutSecs;

    @Bean
    public OpenAiChatModel openAiChatModel(
        @Value("${spring.ai.openai.base-url}") String baseUrl
    ) {
        String apiKey = secrets.getOpenrouterApiKey()
            .orElseThrow(() -> new IllegalStateException("OpenRouter API key is not configured"));
        ClientHttpRequestFactorySettings requestFactorySettings = new ClientHttpRequestFactorySettings(null, Duration.ofSeconds(openAiConnectTimeoutSecs), Duration.ofSeconds(openAiReadTimeoutSecs), null);
        return OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .restClientBuilder(RestClient.builder()
                    .requestFactory(new BufferingClientHttpRequestFactory(
                        ClientHttpRequestFactoryBuilder.of(JdkClientHttpRequestFactory.class).build(requestFactorySettings))
                ))
                .build())
            .build();
    }   
}

package traderalchemy.analyst.client.openrouter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import traderalchemy.analyst.tools.DateTimeTools;


@Slf4j
@Service
public class Openrouter {

    private final ConcurrentHashMap<Model, ModelConfig> modelConfigMap;

    public Openrouter(
        @Value("${spring.ai.openai.chat.options.model.fast}") String fastModel, 
        @Value("${spring.ai.openai.chat.options.model.slow}") String slowModel, 
        DateTimeTools dateTimeTools,
        OpenAiChatModel openAiChatModel
    ) {
        this.modelConfigMap = new ConcurrentHashMap<>();
        OpenAiChatModel model = openAiChatModel.mutate()
            .defaultOptions(OpenAiChatOptions.builder()
                .model(fastModel)
                .temperature(0.7)
                .maxTokens(2048)
                .build())
            .build();
        this.modelConfigMap.put(Model.FAST, new ModelConfig(
            Model.FAST, 
            ChatClient.builder(model)
                .defaultTools(dateTimeTools)
                .defaultAdvisors(createChatLogger(Model.FAST)).build(),10,5));

        model = openAiChatModel.mutate()
            .defaultOptions(OpenAiChatOptions.builder()
                .model(slowModel)
                .temperature(0.7)
                .maxTokens(2048)
                .reasoningEffort("high")
                .build())
            .build();
        this.modelConfigMap.put(Model.SLOW, new ModelConfig(
            Model.SLOW, 
            ChatClient.builder(model)
                .defaultTools(dateTimeTools)
                .defaultAdvisors(createChatLogger(Model.SLOW)).build(),10,2));
    }

    private SimpleLoggerAdvisor createChatLogger(Model model) {
        return new SimpleLoggerAdvisor(req -> "\n== Model: " + model + "==\n" + req.toString(), res -> res.toString(), 0);
    }

    public <T> T request(Model model, Function<ChatClient, T> function) {
        ModelConfig modelConfig = modelConfigMap.get(model);
        while (modelConfig.concurrentRequestsCount.get() >= modelConfig.maxConcurrentRequests
            || modelConfig.refreshAndGetRequestsCountPerSecond() >= modelConfig.maxRequestsPerSecond) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        modelConfig.refreshAndIncrementRequestsCountPerSecond();
        modelConfig.concurrentRequestsCount.incrementAndGet();
        try {
            return function.apply(modelConfig.chatClient);
        } finally {
            modelConfig.concurrentRequestsCount.decrementAndGet();
        }
    }

    public enum Model {
        FAST,
        SLOW
    }

    @RequiredArgsConstructor
    class ModelConfig {
        private final Model model;
        private final ChatClient chatClient;
        private final int maxConcurrentRequests;
        private final int maxRequestsPerSecond;
        private final AtomicInteger concurrentRequestsCount = new AtomicInteger(0);
        private final AtomicInteger requestsCountPerSecond = new AtomicInteger(0);
        private final AtomicLong currentSecond = new AtomicLong(getCurrentSecond());

        public int refreshAndGetRequestsCountPerSecond() {
            long currentSecond = getCurrentSecond();
            if (this.currentSecond.get() != currentSecond) {
                this.currentSecond.set(currentSecond);
                this.requestsCountPerSecond.set(0);
            }
            return this.requestsCountPerSecond.get();
        }

        public int refreshAndIncrementRequestsCountPerSecond() {
            long currentSecond = getCurrentSecond();
            if (this.currentSecond.get() != currentSecond) {
                this.currentSecond.set(currentSecond);
                this.requestsCountPerSecond.set(0);
            }
            return this.requestsCountPerSecond.incrementAndGet();
        }

        public static long getCurrentSecond() {
            return System.currentTimeMillis() / 1000;
        }
    }
}

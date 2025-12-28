package traderalchemy.analyst;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import java.util.UUID;

import traderalchemy.analyst.util.ZonedDateTimeSerializer;

@Slf4j
@Component
public class Global {
    private static volatile boolean IS_RUNNING = true;
    private static final ObjectMapper objectMapper;
    private static final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    static {
        objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        objectMapper.registerModule(javaTimeModule);
    }

    @PreDestroy
    public void destroy() {
        stopRunning();
        shutdownExecutor();
    }

    public static boolean isRunning() {
        return IS_RUNNING;
    }

    public static void stopRunning() {
        IS_RUNNING = false;
    }

    public static ObjectMapper objectMapper() {
        return objectMapper;
    }

    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static ExecutorService executorService() {
        return executorService;
    }

    public static void shutdownExecutor() {
        executorService.shutdown(); // Initiates graceful shutdown
        log.info("global executor shutdown requested");
        long requestTime = System.currentTimeMillis();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Force shutdown if tasks don't complete
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow(); // Force shutdown on interrupt
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        log.info("global executor shutdown completed, time taken: {} ms", System.currentTimeMillis() - requestTime);
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Thread-safe MD5 generation: Creates a new instance per call.
     */
    public static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");  // New instance each time
            md.update(input.getBytes("UTF-8"));
            byte[] digest = md.digest();
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        } catch (Exception e) {
            throw new RuntimeException("Error generating MD5", e);
        }
    }
}

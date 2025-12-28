package traderalchemy.analyst.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
public class Secrets {
    
    private final Optional<String> analystTradebotDbPassword;
    private final Optional<String> openrouterApiKey;

    public Secrets() {
        analystTradebotDbPassword = getDockerSecret("analyst_tradebot_db_password").or(() -> getEnvSecret("TRADEBOT_DB_PASSWORD"));
        openrouterApiKey = getDockerSecret("openrouter_api_key").or(() -> getEnvSecret("OPENROUTER_API_KEY"));
    }

    private Optional<String> getDockerSecret(String secretName) {
        try {
            return Files.lines(Path.of("/run/secrets/" + secretName)).findFirst().map(String::strip);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<String> getEnvSecret(String secretName) {
        return Optional.ofNullable(System.getenv(secretName)).map(String::strip);
    }
}

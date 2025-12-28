package traderalchemy.analyst.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    
    @GetMapping
    public ResponseEntity<String> health() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("DB connection result: {}", result);
            if (result == null || result != 1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB connection failed");
            }
        } catch (Exception e) {
            log.error("DB connection failed, msg: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB connection failed");
        }
        return ResponseEntity.ok("OK");
    }
}

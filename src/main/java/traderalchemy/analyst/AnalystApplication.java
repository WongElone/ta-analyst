package traderalchemy.analyst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AnalystApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalystApplication.class, args);
	}

}

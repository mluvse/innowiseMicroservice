package by.mluvse.userService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class InnowiseMicroserviceApplication {

	static void main(String[] args) {
		SpringApplication.run(InnowiseMicroserviceApplication.class, args);
	}

}

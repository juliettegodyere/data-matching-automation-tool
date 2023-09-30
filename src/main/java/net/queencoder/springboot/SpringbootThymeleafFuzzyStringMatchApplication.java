package net.queencoder.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing //  Enabling JPA Auditing
public class SpringbootThymeleafFuzzyStringMatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootThymeleafFuzzyStringMatchApplication.class, args);
	}

}

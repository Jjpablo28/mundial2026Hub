package co.edu.unbosque.mundialhub;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Mundial2026HubApplication {

	public static void main(String[] args) {
		SpringApplication.run(Mundial2026HubApplication.class, args);
	}
	@Bean
    ModelMapper getModelMapper() {
		return new ModelMapper();
	}

}

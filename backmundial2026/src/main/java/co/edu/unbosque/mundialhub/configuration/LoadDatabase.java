package co.edu.unbosque.mundialhub.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(DatabaseInitializer initializer) {
        return args -> {
            initializer.runDataInitialization();
        };
    }
}
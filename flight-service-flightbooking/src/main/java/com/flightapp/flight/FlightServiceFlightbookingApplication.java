package com.flightapp.flight;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableDiscoveryClient
public class FlightServiceFlightbookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightServiceFlightbookingApplication.class, args);
		
	}
	@Bean
    CommandLineRunner printConfig(Environment env) {
        return args -> {
            System.out.println("======= CONFIG VALUES LOADED =======");
            System.out.println("Application: " + env.getProperty("spring.application.name"));
            System.out.println("Port: " + env.getProperty("server.port"));
            System.out.println("Mongo URI: " + env.getProperty("spring.data.mongodb.uri"));
            System.out.println("====================================");
        };
    }

}

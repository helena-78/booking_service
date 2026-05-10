package com.sportlink.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class BookingServiceApplication {

@Bean
    /*
     * WebClient bean used by BookingService to make synchronous calls to the
     * Scheduling Service (slot reserve / release) and the Search Service
     * (facility lookup).
     */
	public WebClient.Builder getWebClientBuilder() {
		return  WebClient.builder();
	}

	public static void main(String[] args) {
		DatabaseInitializer.initialize("bookingservice_db");
		SpringApplication.run(BookingServiceApplication.class, args);
	}

}

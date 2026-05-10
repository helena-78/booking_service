package com.sportlink.scheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SchedulingServiceApplication {

	public static void main(String[] args) {
		DatabaseInitializer.initialize("scheduling_db");
		SpringApplication.run(SchedulingServiceApplication.class, args);
	}

}

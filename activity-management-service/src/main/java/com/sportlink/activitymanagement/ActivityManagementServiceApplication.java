package com.sportlink.activitymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ActivityManagementServiceApplication {

	public static void main(String[] args) {
		DatabaseInitializer.initialize("activity_management_db");
		SpringApplication.run(ActivityManagementServiceApplication.class, args);
	}

}

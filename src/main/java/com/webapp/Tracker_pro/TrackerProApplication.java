package com.webapp.Tracker_pro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync //Enable Async --(for non-blocking email sending)
public class TrackerProApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackerProApplication.class, args);
	}

}

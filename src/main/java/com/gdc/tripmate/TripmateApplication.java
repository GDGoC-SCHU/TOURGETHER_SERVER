package com.gdc.tripmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TripmateApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripmateApplication.class, args);
	}

}

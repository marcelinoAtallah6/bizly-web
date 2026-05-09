package com.pm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class PmApplication {

	public static void main(String[] args) {
		SpringApplication.run(PmApplication.class, args);
	}

}

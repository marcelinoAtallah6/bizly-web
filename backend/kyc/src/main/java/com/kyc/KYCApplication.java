package com.kyc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class KYCApplication {

	public static void main(String[] args) {
		SpringApplication.run(KYCApplication.class, args);
	}

}

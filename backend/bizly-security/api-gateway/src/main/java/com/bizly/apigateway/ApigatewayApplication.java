package com.bizly.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.bizly.apigateway.workflow.BizlyWorkflowGatewayProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(BizlyWorkflowGatewayProperties.class)
public class ApigatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApigatewayApplication.class, args);
	}

}

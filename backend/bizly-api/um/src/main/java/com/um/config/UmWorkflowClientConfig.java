package com.um.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UmWorkflowClientConfig {

	@Bean
	@LoadBalanced
	public RestTemplate workflowRestTemplate() {
		return new RestTemplate();
	}
}

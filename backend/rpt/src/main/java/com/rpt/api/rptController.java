package com.rpt.api;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class rptController {
	
	@GetMapping("/get")
	public String home() {
		return "<h2>Microservice two is running</h2>";
	}

}

package com.um.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController	
public class umController {
	
	@GetMapping("/get")
	public String home() {
		System.out.println(" in >>>>>>>>>");
		return "<h2>Microservice one is running</h2>";
	}

}

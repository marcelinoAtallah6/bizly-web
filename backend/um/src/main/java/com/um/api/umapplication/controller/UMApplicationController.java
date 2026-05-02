package com.um.api.umapplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.umapplication.model.UMGroupApplication;
import com.um.api.umapplication.service.UMApplicationService;

@RestController
public class UMApplicationController {

	@Autowired
	private UMApplicationService umApplicationService;

	@GetMapping("/getAllApplications")
	public List<UMGroupApplication> getAllApplications() {
		return umApplicationService.getAllApplications();
	}

	@PostMapping("/saveApplication")
	public String saveApplication() {
		System.out.println(" in >>>>>>>>>");
		return "<h2>saveApplication</h2>";
	}

}

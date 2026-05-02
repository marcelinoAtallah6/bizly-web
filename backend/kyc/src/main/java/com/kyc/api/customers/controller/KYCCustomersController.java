package com.kyc.api.customers.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kyc.api.customers.dto.KYCCustomersDTO;
import com.kyc.api.customers.service.KYCCustomersService;
import com.kyc.config.ApiResponse;

@RestController
public class KYCCustomersController {

	@Autowired
	private KYCCustomersService kycCustomersService;

	@GetMapping("/customers")
	public ResponseEntity<ApiResponse<List<KYCCustomersDTO>>> getAllApplications() {
		try {
		return ResponseEntity.ok(new ApiResponse<>("success", kycCustomersService.getAllCustomers(),
				"Customer retrieved successfully."));
		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
		}
	}

	@PostMapping("/customers")
	public ResponseEntity<ApiResponse<KYCCustomersDTO>> addCustomer(@RequestBody KYCCustomersDTO customerDTO) {
		try {
			return ResponseEntity.ok(new ApiResponse<>("success", kycCustomersService.addCustomer(customerDTO),
					"Customer craeted successfully."));

		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
		}
	}

	@PutMapping("/customers/{id}")
	public ResponseEntity<ApiResponse<KYCCustomersDTO>> updateCustomer(@PathVariable Long id,
			@RequestBody KYCCustomersDTO customerDTO) {
		try {
			return ResponseEntity.ok(new ApiResponse<>("success", kycCustomersService.updateCustomer(id, customerDTO),
					"Customer updated successfully."));

		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
		}

	}

	@DeleteMapping("/customers/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
		try {
			kycCustomersService.deleteCustomer(id);
			return ResponseEntity.ok(new ApiResponse<>("success", null,
					"Customer deleted successfully."));		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
		}
	}

}

package com.kyc.api.customers.service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kyc.api.customers.dto.KYCCustomersDTO;
import com.kyc.api.customers.model.KYCCustomers;
import com.kyc.api.customers.model.KYCDocument;
import com.kyc.api.customers.repository.KYCCustomersRepository;

@Service
public class KYCCustomersService {

	@Autowired
	private KYCCustomersRepository kycCustomersRepository;

	public List<KYCCustomersDTO> getAllCustomers() {
		return kycCustomersRepository.findAll().stream().map(KYCCustomersDTO::new).collect(Collectors.toList());
	}

	public KYCCustomersDTO addCustomer(KYCCustomersDTO customerDTO) {

		KYCCustomers customer = new KYCCustomers(customerDTO);
		try {

			if (customer.getDocuments() != null) {
				for (KYCDocument document : customer.getDocuments()) {
					document.setCustomer(customer);
					if (document.getDocumentFile() != null) {
						try {
							document.setDocumentFile(Base64.getDecoder().decode(document.getDocumentFile()));
						} catch (IllegalArgumentException e) {
							// Handle the error if the Base64 string is invalid
							throw new IllegalArgumentException("Invalid Base64 input for document data", e);
						}
					}

				}
			}

			return new KYCCustomersDTO(kycCustomersRepository.save(customer));

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return new KYCCustomersDTO(kycCustomersRepository.save(customer));

		}
	}

	public KYCCustomersDTO updateCustomer(Long id, KYCCustomersDTO customerDTO) {
		KYCCustomers customer = kycCustomersRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Customer not found"));
		customer.updateFromDTO(customerDTO);

		if (customer.getDocuments() != null) {
			for (KYCDocument document : customer.getDocuments()) {
				document.setCustomer(customer);
				if (document.getDocumentFile() != null) {
					try {
						document.setDocumentFile(Base64.getDecoder().decode(document.getDocumentFile()));
					} catch (IllegalArgumentException e) {
						// Handle the error if the Base64 string is invalid
						throw new IllegalArgumentException("Invalid Base64 input for document data", e);
					}
				}

			}
		}
		return new KYCCustomersDTO(kycCustomersRepository.save(customer));
	}

	public void deleteCustomer(Long id) {
		kycCustomersRepository.deleteById(id);
	}

}

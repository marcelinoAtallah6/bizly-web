package com.kyc.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.kyc.api.dto.add.AddCustomerRequest;
import com.kyc.api.dto.add.AddCustomerResponse;
import com.kyc.api.dto.delete.DeleteCustomerRequest;
import com.kyc.api.dto.delete.DeleteCustomerResponse;
import com.kyc.api.dto.get.GetCustomerRequest;
import com.kyc.api.dto.get.GetCustomerResponse;
import com.kyc.api.dto.gets.GetsCustomersRequest;
import com.kyc.api.dto.update.UpdateCustomerRequest;
import com.kyc.api.dto.update.UpdateCustomerResponse;
import com.kyc.api.model.customer.KycCustomer;
import com.kyc.api.repository.KycCustomerRepository;
import com.kyc.common.ApiMessages;
import com.kyc.common.PageResponse;
import com.kyc.exception.ServiceException;
import com.kyc.security.BusinessContextHolder;

@Service
public class CustomerServiceImpl implements ICustomerService {

	@Autowired
	private KycCustomerRepository repository;

	@Override
	public AddCustomerResponse add(AddCustomerRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();

		KycCustomer customer = new KycCustomer();
		mapRequestOntoEntity(request, customer);
		customer.setFullName(request.getFirstName() + " " + request.getLastName());
		customer.setBusinessId(businessId);
		customer.setCreatedAt(LocalDateTime.now());
		customer.setNotifWelcomeFlag(0);
		customer.setNotifWelcomeStatus(0);

		repository.save(customer);

		AddCustomerResponse response = new AddCustomerResponse();
		response.setId(customer.getId());
		return response;
	}

	@Override
	public UpdateCustomerResponse update(UpdateCustomerRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		KycCustomer customer = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CUSTOMER_NOT_FOUND, HttpStatus.NOT_FOUND));

		mapRequestOntoEntity(request, customer);
		customer.setFullName(request.getFirstName() + " " + request.getLastName());

		repository.save(customer);

		UpdateCustomerResponse response = new UpdateCustomerResponse();
		response.setId(customer.getId());
		return response;
	}

	private void mapRequestOntoEntity(AddCustomerRequest request, KycCustomer customer) {
		mapCommonFields(request.getFirstName(), request.getLastName(), request.getDob(), request.getEmail(),
				request.getMobileNumber(), request.getAddressLine1(), request.getAddressLine2(), request.getCity(),
				request.getStateProvince(), request.getPostalCode(), request.getCountry(), request.getCustomerStatus(),
				customer);
	}

	private void mapRequestOntoEntity(UpdateCustomerRequest request, KycCustomer customer) {
		mapCommonFields(request.getFirstName(), request.getLastName(), request.getDob(), request.getEmail(),
				request.getMobileNumber(), request.getAddressLine1(), request.getAddressLine2(), request.getCity(),
				request.getStateProvince(), request.getPostalCode(), request.getCountry(), request.getCustomerStatus(),
				customer);
	}

	private void mapCommonFields(String firstName, String lastName, LocalDate dob, String email,
			String mobileNumber, String addressLine1, String addressLine2, String city, String stateProvince,
			String postalCode, String country, String customerStatus, KycCustomer customer) {
		customer.setFirstName(firstName);
		customer.setLastName(lastName);
		customer.setDob(dob);
		customer.setEmail(email);
		customer.setMobileNumber(mobileNumber);
		customer.setAddressLine1(trimToNull(addressLine1));
		customer.setAddressLine2(trimToNull(addressLine2));
		customer.setCity(trimToNull(city));
		customer.setStateProvince(trimToNull(stateProvince));
		customer.setPostalCode(trimToNull(postalCode));
		customer.setCountry(trimToNull(country));
		customer.setCustomerStatus(customerStatus);
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	@Override
	public DeleteCustomerResponse delete(DeleteCustomerRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		KycCustomer customer = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CUSTOMER_NOT_FOUND, HttpStatus.NOT_FOUND));

		repository.delete(customer);

		DeleteCustomerResponse response = new DeleteCustomerResponse();
		response.setId(request.getId());
		return response;
	}

	@Override
	public GetCustomerResponse get(GetCustomerRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		KycCustomer customer = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CUSTOMER_NOT_FOUND, HttpStatus.NOT_FOUND));

		return buildResponse(customer);
	}

	@Override
	public PageResponse<GetCustomerResponse> gets(GetsCustomersRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<KycCustomer> page = repository.findAllByBusinessId(businessId, pageable);

		List<GetCustomerResponse> items = page.getContent().stream().map(this::buildResponse).collect(Collectors.toList());

		PageResponse<GetCustomerResponse> response = new PageResponse<>();
		response.setItems(items);
		response.setTotalCount(page.getTotalElements());
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalPages(page.getTotalPages());
		return response;
	}

	private GetCustomerResponse buildResponse(KycCustomer customer) {
		GetCustomerResponse response = new GetCustomerResponse();
		response.setId(customer.getId());
		response.setFirstName(customer.getFirstName());
		response.setLastName(customer.getLastName());
		response.setFullName(customer.getFullName());
		response.setDob(customer.getDob());
		response.setEmail(customer.getEmail());
		response.setMobileNumber(customer.getMobileNumber());
		response.setAddressLine1(customer.getAddressLine1());
		response.setAddressLine2(customer.getAddressLine2());
		response.setCity(customer.getCity());
		response.setStateProvince(customer.getStateProvince());
		response.setPostalCode(customer.getPostalCode());
		response.setCountry(customer.getCountry());
		response.setCustomerStatus(customer.getCustomerStatus());
		response.setCreatedAt(customer.getCreatedAt());
		return response;
	}
}

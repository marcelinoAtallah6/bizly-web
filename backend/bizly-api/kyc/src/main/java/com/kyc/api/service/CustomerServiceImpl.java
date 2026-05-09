package com.kyc.api.service;

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
import com.kyc.api.dto.model.CustomerDetailRequest;
import com.kyc.api.dto.model.CustomerDetailResponse;
import com.kyc.api.dto.update.UpdateCustomerRequest;
import com.kyc.api.dto.update.UpdateCustomerResponse;
import com.kyc.api.model.customer.KycCustomer;
import com.kyc.api.model.customer.KycCustomerDetail;
import com.kyc.api.repository.KycCustomerRepository;
import com.kyc.common.ApiMessages;
import com.kyc.common.PageResponse;
import com.kyc.exception.ServiceException;

@Service
public class CustomerServiceImpl implements ICustomerService {

	@Autowired
	private KycCustomerRepository repository;

	@Override
	public AddCustomerResponse add(AddCustomerRequest request) {

		KycCustomer customer = new KycCustomer();
		customer.setFirstName(request.getFirstName());
		customer.setLastName(request.getLastName());
		customer.setFullName(request.getFirstName() + " " + request.getLastName());
		customer.setDob(request.getDob());
		customer.setEmail(request.getEmail());
		customer.setMobileNumber(request.getMobileNumber());
		customer.setCreatedAt(LocalDateTime.now());
		customer.setDetails(toDetails(request.getDetails(), customer));

		repository.save(customer);

		AddCustomerResponse response = new AddCustomerResponse();
		response.setId(customer.getId());
		return response;
	}

	@Override
	public UpdateCustomerResponse update(UpdateCustomerRequest request) {

		KycCustomer customer = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.CUSTOMER_NOT_FOUND, HttpStatus.NOT_FOUND));

		customer.setFirstName(request.getFirstName());
		customer.setLastName(request.getLastName());
		customer.setFullName(request.getFirstName() + " " + request.getLastName());
		customer.setDob(request.getDob());
		customer.setEmail(request.getEmail());
		customer.setMobileNumber(request.getMobileNumber());

		if (request.getDetails() != null) {
			customer.setDetails(toDetails(request.getDetails(), customer));
		}

		repository.save(customer);

		UpdateCustomerResponse response = new UpdateCustomerResponse();
		response.setId(customer.getId());
		return response;
	}

	@Override
	public DeleteCustomerResponse delete(DeleteCustomerRequest request) {

		if (!repository.existsById(request.getId())) {
			throw new ServiceException(ApiMessages.CUSTOMER_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		repository.deleteById(request.getId());

		DeleteCustomerResponse response = new DeleteCustomerResponse();
		response.setId(request.getId());
		return response;
	}

	@Override
	public GetCustomerResponse get(GetCustomerRequest request) {

		KycCustomer customer = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.CUSTOMER_NOT_FOUND, HttpStatus.NOT_FOUND));

		return buildResponse(customer);
	}

	@Override
	public PageResponse<GetCustomerResponse> gets(GetsCustomersRequest request) {

		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<KycCustomer> page = repository.findAll(pageable);

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
		response.setCreatedAt(customer.getCreatedAt());

		if (customer.getDetails() != null) {
			response.setDetails(customer.getDetails().stream().map(detail -> {
				CustomerDetailResponse detailResponse = new CustomerDetailResponse();
				detailResponse.setId(detail.getId());
				detailResponse.setFieldName(detail.getFieldName());
				detailResponse.setFieldValue(detail.getFieldValue());
				detailResponse.setCustomerStatus(detail.getCustomerStatus());
				detailResponse.setCreatedAt(detail.getCreatedAt());
				return detailResponse;
			}).collect(Collectors.toList()));
		}

		return response;
	}

	private List<KycCustomerDetail> toDetails(List<CustomerDetailRequest> details, KycCustomer customer) {
		if (details == null) {
			return null;
		}

		return details.stream().map(request -> {
			KycCustomerDetail detail = new KycCustomerDetail();
			detail.setCustomer(customer);
			detail.setFieldName(request.getFieldName());
			detail.setFieldValue(request.getFieldValue());
			detail.setCustomerStatus(request.getCustomerStatus());
			detail.setCreatedAt(LocalDateTime.now());
			return detail;
		}).collect(Collectors.toList());
	}
}

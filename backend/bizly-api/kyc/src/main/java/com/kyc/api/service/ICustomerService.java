package com.kyc.api.service;

import com.kyc.api.dto.add.AddCustomerRequest;
import com.kyc.api.dto.add.AddCustomerResponse;
import com.kyc.api.dto.delete.DeleteCustomerRequest;
import com.kyc.api.dto.delete.DeleteCustomerResponse;
import com.kyc.api.dto.get.GetCustomerRequest;
import com.kyc.api.dto.get.GetCustomerResponse;
import com.kyc.api.dto.gets.GetsCustomersRequest;
import com.kyc.api.dto.update.UpdateCustomerRequest;
import com.kyc.api.dto.update.UpdateCustomerResponse;
import com.kyc.common.PageResponse;

public interface ICustomerService {

	AddCustomerResponse add(AddCustomerRequest request);

	UpdateCustomerResponse update(UpdateCustomerRequest request);

	DeleteCustomerResponse delete(DeleteCustomerRequest request);

	GetCustomerResponse get(GetCustomerRequest request);

	PageResponse<GetCustomerResponse> gets(GetsCustomersRequest request);
}

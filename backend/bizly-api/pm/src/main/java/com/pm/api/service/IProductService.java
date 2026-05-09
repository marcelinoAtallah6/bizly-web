package com.pm.api.service;

import com.pm.api.dto.add.AddProductRequest;
import com.pm.api.dto.add.AddProductResponse;
import com.pm.api.dto.delete.DeleteProductRequest;
import com.pm.api.dto.delete.DeleteProductResponse;
import com.pm.api.dto.get.GetProductRequest;
import com.pm.api.dto.get.GetProductResponse;
import com.pm.api.dto.gets.GetsProductsRequest;
import com.pm.api.dto.update.UpdateProductRequest;
import com.pm.api.dto.update.UpdateProductResponse;
import com.pm.common.PageResponse;

public interface IProductService {

	public AddProductResponse add(AddProductRequest request);

	public UpdateProductResponse update(UpdateProductRequest request);

	public DeleteProductResponse delete(DeleteProductRequest request);

	public GetProductResponse get(GetProductRequest request);

	public PageResponse<GetProductResponse> gets(GetsProductsRequest request);
}

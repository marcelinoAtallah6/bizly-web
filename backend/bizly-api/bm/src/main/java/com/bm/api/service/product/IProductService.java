package com.bm.api.service.product;

import com.bm.api.dto.product.add.AddProductRequest;
import com.bm.api.dto.product.add.AddProductResponse;
import com.bm.api.dto.product.delete.DeleteProductRequest;
import com.bm.api.dto.product.delete.DeleteProductResponse;
import com.bm.api.dto.product.get.GetProductRequest;
import com.bm.api.dto.product.get.GetProductResponse;
import com.bm.api.dto.product.gets.GetsProductsRequest;
import com.bm.api.dto.product.update.UpdateProductRequest;
import com.bm.api.dto.product.update.UpdateProductResponse;
import com.bm.common.PageResponse;

public interface IProductService {

	public AddProductResponse add(AddProductRequest request);

	public UpdateProductResponse update(UpdateProductRequest request);

	public DeleteProductResponse delete(DeleteProductRequest request);

	public GetProductResponse get(GetProductRequest request);

	public PageResponse<GetProductResponse> gets(GetsProductsRequest request);
}

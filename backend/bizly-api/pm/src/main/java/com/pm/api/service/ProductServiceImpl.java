package com.pm.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.pm.api.dto.add.AddProductRequest;
import com.pm.api.dto.add.AddProductResponse;
import com.pm.api.dto.delete.DeleteProductRequest;
import com.pm.api.dto.delete.DeleteProductResponse;
import com.pm.api.dto.get.GetProductRequest;
import com.pm.api.dto.get.GetProductResponse;
import com.pm.api.dto.gets.GetsProductsRequest;
import com.pm.api.dto.update.UpdateProductRequest;
import com.pm.api.dto.update.UpdateProductResponse;
import com.pm.api.model.Product;
import com.pm.api.repository.ProductRepository;
import com.pm.common.ApiMessages;
import com.pm.common.PageResponse;
import com.pm.exception.ServiceException;

@Service
public class ProductServiceImpl implements IProductService {

	@Autowired
	private ProductRepository repository;

	@Override
	public AddProductResponse add(AddProductRequest request) {

		Product p = new Product();
		p.setName(request.getName());
		p.setPrice(request.getPrice());

		repository.save(p);

		return new AddProductResponse();
	}

	@Override
	public UpdateProductResponse update(UpdateProductRequest request) {

		Product p = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

		p.setName(request.getName());
		p.setPrice(request.getPrice());

		repository.save(p);

		return new UpdateProductResponse();
	}

	@Override
	public DeleteProductResponse delete(DeleteProductRequest request) {

		repository.deleteById(request.getId());

		return new DeleteProductResponse();
	}

	@Override
	public GetProductResponse get(GetProductRequest request) {

		Product p = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

		GetProductResponse res = new GetProductResponse();
		res.setId(p.getId());
		res.setName(p.getName());
		res.setPrice(p.getPrice());

		return res;
	}

	@Override
	public PageResponse<GetProductResponse> gets(GetsProductsRequest request) {

		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());

		Page<Product> page = repository.findAll(pageable);

		List<GetProductResponse> list = page.getContent().stream().map(p -> {
			GetProductResponse res = new GetProductResponse();
			res.setId(p.getId());
			res.setName(p.getName());
			res.setPrice(p.getPrice());
			return res;
		}).collect(Collectors.toList());

		PageResponse<GetProductResponse> response = new PageResponse<>();
		response.setItems(list);
		response.setTotalCount(page.getTotalElements());
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalPages(page.getTotalPages());

		return response;
	}
}
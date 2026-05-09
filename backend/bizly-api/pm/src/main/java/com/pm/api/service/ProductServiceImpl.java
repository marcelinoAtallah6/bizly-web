package com.pm.api.service;

import java.time.LocalDateTime;
import java.util.Base64;
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
import com.pm.common.ProductImageUtil;
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
		p.setCreatedAt(LocalDateTime.now());
		if (request.getStockQuantity() != null) {
			p.setStockQuantity(request.getStockQuantity());
		}
		applyOptionalProductImageOnCreate(p, request.getProductImageMimeType(), request.getProductImageBase64());

		repository.save(p);

		AddProductResponse res = new AddProductResponse();
		res.setId(p.getId());
		return res;
	}

	@Override
	public UpdateProductResponse update(UpdateProductRequest request) {

		Product p = repository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

		p.setName(request.getName());
		p.setPrice(request.getPrice());
		if (request.getStockQuantity() != null) {
			p.setStockQuantity(request.getStockQuantity());
		}
		applyProductImageOnUpdate(p, request);

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

		return mapToResponse(p, true);
	}

	@Override
	public PageResponse<GetProductResponse> gets(GetsProductsRequest request) {

		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());

		Page<Product> page = repository.findAll(pageable);

		boolean includeImages = Boolean.TRUE.equals(request.getIncludeImages());

		List<GetProductResponse> list = page.getContent().stream().map(p -> mapToResponse(p, includeImages))
				.collect(Collectors.toList());

		PageResponse<GetProductResponse> response = new PageResponse<>();
		response.setItems(list);
		response.setTotalCount(page.getTotalElements());
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalPages(page.getTotalPages());

		return response;
	}

	private GetProductResponse mapToResponse(Product p, boolean includeImage) {
		GetProductResponse res = new GetProductResponse();
		res.setId(p.getId());
		res.setName(p.getName());
		res.setPrice(p.getPrice());
		res.setStockQuantity(p.getStockQuantity());
		if (includeImage && p.getProductImageData() != null && p.getProductImageData().length > 0) {
			res.setProductImageMimeType(p.getProductImageMime());
			res.setProductImageBase64(Base64.getEncoder().encodeToString(p.getProductImageData()));
		}
		return res;
	}

	private static void applyOptionalProductImageOnCreate(Product p, String mimeType, String base64) {
		if (base64 == null || base64.isBlank()) {
			return;
		}
		ProductImageUtil.validateMime(mimeType);
		p.setProductImageMime(mimeType.trim());
		p.setProductImageData(ProductImageUtil.decodeBase64Image(base64));
	}

	private static void applyProductImageOnUpdate(Product p, UpdateProductRequest request) {
		if (Boolean.TRUE.equals(request.getClearProductImage())) {
			p.setProductImageMime(null);
			p.setProductImageData(null);
			return;
		}
		if (request.getProductImageBase64() != null && !request.getProductImageBase64().isBlank()) {
			ProductImageUtil.validateMime(request.getProductImageMimeType());
			p.setProductImageMime(request.getProductImageMimeType().trim());
			p.setProductImageData(ProductImageUtil.decodeBase64Image(request.getProductImageBase64()));
		}
	}
}

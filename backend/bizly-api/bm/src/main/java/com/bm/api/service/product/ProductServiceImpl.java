package com.bm.api.service.product;

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

import com.bm.api.dto.product.add.AddProductRequest;
import com.bm.api.dto.product.add.AddProductResponse;
import com.bm.api.dto.product.delete.DeleteProductRequest;
import com.bm.api.dto.product.delete.DeleteProductResponse;
import com.bm.api.dto.product.get.GetProductRequest;
import com.bm.api.dto.product.get.GetProductResponse;
import com.bm.api.dto.product.gets.GetsProductsRequest;
import com.bm.api.dto.product.update.UpdateProductRequest;
import com.bm.api.dto.product.update.UpdateProductResponse;
import com.bm.api.model.Product;
import com.bm.api.repository.ProductRepository;
import com.bm.common.ApiMessages;
import com.bm.common.PageResponse;
import com.bm.common.ProductImageUtil;
import com.bm.exception.ServiceException;
import com.bm.security.BusinessContextHolder;

@Service
public class ProductServiceImpl implements IProductService {

	@Autowired
	private ProductRepository repository;

	@Override
	public AddProductResponse add(AddProductRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();

		Product p = new Product();
		p.setName(request.getName());
		p.setPrice(request.getPrice());
		p.setCreatedAt(LocalDateTime.now());
		p.setBusinessId(businessId);
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

		Long businessId = BusinessContextHolder.requireBusinessId();
		Product p = repository.findByIdAndBusinessId(request.getId(), businessId)
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

		Long businessId = BusinessContextHolder.requireBusinessId();
		Product p = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));
		repository.delete(p);

		return new DeleteProductResponse();
	}

	@Override
	public GetProductResponse get(GetProductRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		Product p = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

		return mapToResponse(p, true);
	}

	@Override
	public PageResponse<GetProductResponse> gets(GetsProductsRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());

		Page<Product> page = repository.findAllByBusinessId(businessId, pageable);

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

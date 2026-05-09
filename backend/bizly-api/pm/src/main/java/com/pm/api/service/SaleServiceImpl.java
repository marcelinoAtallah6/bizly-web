package com.pm.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pm.api.dto.sale.CheckoutLineRequest;
import com.pm.api.dto.sale.CheckoutRequest;
import com.pm.api.dto.sale.CheckoutResponse;
import com.pm.api.dto.sale.GetSaleRequest;
import com.pm.api.dto.sale.GetSaleResponse;
import com.pm.api.dto.sale.GetsSalesRequest;
import com.pm.api.dto.sale.SaleLineResponse;
import com.pm.api.dto.sale.SaleSummaryResponse;
import com.pm.api.model.CustomerSale;
import com.pm.api.model.CustomerSaleLine;
import com.pm.api.model.Product;
import com.pm.api.repository.CustomerSaleRepository;
import com.pm.api.repository.ProductRepository;
import com.pm.common.ApiMessages;
import com.pm.common.PageResponse;
import com.pm.exception.ServiceException;

@Service
public class SaleServiceImpl implements ISaleService {

	@Autowired
	private CustomerSaleRepository saleRepository;

	@Autowired
	private ProductRepository productRepository;

	@Override
	@Transactional
	public CheckoutResponse checkout(CheckoutRequest request) {
		Map<Long, Integer> mergedQty = new LinkedHashMap<>();
		for (CheckoutLineRequest line : request.getLines()) {
			if (line.getQuantity() == null || line.getQuantity() < 1) {
				throw new ServiceException(ApiMessages.CHECKOUT_INVALID, HttpStatus.BAD_REQUEST);
			}
			mergedQty.merge(line.getProductId(), line.getQuantity(), Integer::sum);
		}

		for (Map.Entry<Long, Integer> e : mergedQty.entrySet()) {
			Product p = productRepository.findById(e.getKey())
					.orElseThrow(() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.BAD_REQUEST));
			Integer stock = p.getStockQuantity();
			int qty = e.getValue();
			if (stock != null && qty > stock) {
				throw new ServiceException(ApiMessages.INSUFFICIENT_STOCK, HttpStatus.BAD_REQUEST);
			}
		}

		double total = 0d;
		CustomerSale sale = new CustomerSale();
		sale.setCustomerId(request.getCustomerId());
		if (request.getCustomerDisplayName() != null) {
			String label = request.getCustomerDisplayName().trim();
			sale.setCustomerDisplayName(label.isEmpty() ? null : label.substring(0, Math.min(label.length(), 300)));
		}
		sale.setStatus("COMPLETED");
		sale.setCreatedAt(LocalDateTime.now());
		sale.setLines(new ArrayList<>());

		for (Map.Entry<Long, Integer> e : mergedQty.entrySet()) {
			Product p = productRepository.findById(e.getKey()).orElseThrow(
					() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.BAD_REQUEST));
			int qty = e.getValue();
			double unit = p.getPrice() != null ? p.getPrice() : 0d;
			double lineTotal = unit * qty;
			total += lineTotal;

			CustomerSaleLine sl = new CustomerSaleLine();
			sl.setSale(sale);
			sl.setProductId(p.getId());
			sl.setProductName(p.getName());
			sl.setQuantity(qty);
			sl.setUnitPrice(unit);
			sale.getLines().add(sl);
		}

		sale.setTotalAmount(total);
		saleRepository.save(sale);

		for (Map.Entry<Long, Integer> e : mergedQty.entrySet()) {
			Product p = productRepository.findById(e.getKey()).orElseThrow(
					() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.BAD_REQUEST));
			Integer stock = p.getStockQuantity();
			if (stock != null) {
				p.setStockQuantity(stock - e.getValue());
				productRepository.save(p);
			}
		}

		CheckoutResponse res = new CheckoutResponse();
		res.setSaleId(sale.getId());
		res.setTotalAmount(sale.getTotalAmount());
		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public GetSaleResponse get(GetSaleRequest request) {
		CustomerSale sale = saleRepository.findById(request.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.SALE_NOT_FOUND, HttpStatus.NOT_FOUND));
		sale.getLines().size();

		GetSaleResponse res = new GetSaleResponse();
		res.setId(sale.getId());
		res.setCustomerId(sale.getCustomerId());
		res.setCustomerDisplayName(sale.getCustomerDisplayName());
		res.setTotalAmount(sale.getTotalAmount());
		res.setStatus(sale.getStatus());
		res.setCreatedAt(sale.getCreatedAt());
		res.setLines(sale.getLines().stream().map(this::toLineResponse).collect(Collectors.toList()));
		return res;
	}

	private SaleLineResponse toLineResponse(CustomerSaleLine l) {
		SaleLineResponse r = new SaleLineResponse();
		r.setProductId(l.getProductId());
		r.setProductName(l.getProductName());
		r.setQuantity(l.getQuantity());
		r.setUnitPrice(l.getUnitPrice());
		if (l.getUnitPrice() != null && l.getQuantity() != null) {
			r.setLineTotal(l.getUnitPrice() * l.getQuantity());
		}
		return r;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<SaleSummaryResponse> gets(GetsSalesRequest request) {
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize(),
				Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<CustomerSale> page;
		if (request.getCustomerId() != null) {
			page = saleRepository.findByCustomerIdOrderByCreatedAtDesc(request.getCustomerId(), pageable);
		} else {
			page = saleRepository.findAll(pageable);
		}
		List<SaleSummaryResponse> items = page.getContent().stream().map(s -> {
			SaleSummaryResponse r = new SaleSummaryResponse();
			r.setId(s.getId());
			r.setCustomerId(s.getCustomerId());
			r.setCustomerDisplayName(s.getCustomerDisplayName());
			r.setTotalAmount(s.getTotalAmount());
			r.setStatus(s.getStatus());
			r.setCreatedAt(s.getCreatedAt());
			return r;
		}).collect(Collectors.toList());
		PageResponse<SaleSummaryResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}
}

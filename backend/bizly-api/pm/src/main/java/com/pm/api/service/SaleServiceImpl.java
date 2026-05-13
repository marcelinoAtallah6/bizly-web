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
import com.pm.api.model.ServiceItem;
import com.pm.api.repository.CustomerSaleRepository;
import com.pm.api.repository.ProductRepository;
import com.pm.api.repository.ServiceItemRepository;
import com.pm.common.ApiMessages;
import com.pm.common.PageResponse;
import com.pm.exception.ServiceException;
import com.pm.security.BusinessContextHolder;

/**
 * Unified product + service checkout. Lines are validated, priced, and persisted to
 * {@code pm_customer_sale} / {@code pm_customer_sale_line}. Service lines are looked up against BM's
 * {@code bm_service_item} table via the read-only {@link ServiceItemRepository}, so the {@code /pm/sale/*}
 * endpoints can serve every kind of cart the payments screen submits — there is no longer a need to
 * fan out to the BM sale controller.
 */
@Service
public class SaleServiceImpl implements ISaleService {

	private static final String LINE_PRODUCT = "PRODUCT";
	private static final String LINE_SERVICE = "SERVICE";

	@Autowired
	private CustomerSaleRepository saleRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ServiceItemRepository serviceItemRepository;

	@Override
	@Transactional
	public CheckoutResponse checkout(CheckoutRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Map<Long, Integer> mergedProductQty = new LinkedHashMap<>();
		Map<Long, Integer> mergedServiceQty = new LinkedHashMap<>();

		for (CheckoutLineRequest line : request.getLines()) {
			if (line.getQuantity() == null || line.getQuantity() < 1) {
				throw new ServiceException(ApiMessages.CHECKOUT_INVALID, HttpStatus.BAD_REQUEST);
			}
			Long pid = line.getProductId();
			Long sid = line.getServiceId();
			boolean hasProduct = pid != null;
			boolean hasService = sid != null;
			if (hasProduct == hasService) {
				throw new ServiceException("Each line must have exactly one of productId or serviceId",
						HttpStatus.BAD_REQUEST);
			}
			if (hasProduct) {
				if (pid <= 0L) {
					throw new ServiceException(ApiMessages.CHECKOUT_INVALID, HttpStatus.BAD_REQUEST);
				}
				mergedProductQty.merge(pid, line.getQuantity(), Integer::sum);
			} else {
				if (sid <= 0L) {
					throw new ServiceException(ApiMessages.CHECKOUT_INVALID, HttpStatus.BAD_REQUEST);
				}
				mergedServiceQty.merge(sid, line.getQuantity(), Integer::sum);
			}
		}

		// Validate stock for product lines up front so we fail the whole cart cleanly if any line
		// would oversell.
		for (Map.Entry<Long, Integer> e : mergedProductQty.entrySet()) {
			Product p = productRepository.findByIdAndBusinessId(e.getKey(), businessId)
					.orElseThrow(() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.BAD_REQUEST));
			Integer stock = p.getStockQuantity();
			int qty = e.getValue();
			if (stock != null && qty > stock) {
				throw new ServiceException(ApiMessages.INSUFFICIENT_STOCK, HttpStatus.BAD_REQUEST);
			}
		}

		// Validate service items exist and are active.
		for (Long serviceId : mergedServiceQty.keySet()) {
			ServiceItem s = serviceItemRepository.findByIdAndBusinessId(serviceId, businessId)
					.orElseThrow(() -> new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND,
							HttpStatus.BAD_REQUEST));
			if (!Boolean.TRUE.equals(s.getActive())) {
				throw new ServiceException(ApiMessages.SERVICE_ITEM_INACTIVE, HttpStatus.BAD_REQUEST);
			}
		}

		double total = 0d;
		CustomerSale sale = new CustomerSale();
		sale.setBusinessId(businessId);
		sale.setCustomerId(request.getCustomerId());
		if (request.getCustomerDisplayName() != null) {
			String label = request.getCustomerDisplayName().trim();
			sale.setCustomerDisplayName(label.isEmpty() ? null : label.substring(0, Math.min(label.length(), 300)));
		}
		sale.setStatus("COMPLETED");
		sale.setCreatedAt(LocalDateTime.now());
		sale.setLines(new ArrayList<>());

		for (Map.Entry<Long, Integer> e : mergedProductQty.entrySet()) {
			Product p = productRepository.findByIdAndBusinessId(e.getKey(), businessId).orElseThrow(
					() -> new ServiceException(ApiMessages.PRODUCT_NOT_FOUND, HttpStatus.BAD_REQUEST));
			int qty = e.getValue();
			double unit = p.getPrice() != null ? p.getPrice() : 0d;
			double lineTotal = unit * qty;
			total += lineTotal;

			CustomerSaleLine sl = new CustomerSaleLine();
			sl.setSale(sale);
			sl.setBusinessId(businessId);
			sl.setLineType(LINE_PRODUCT);
			sl.setProductId(p.getId());
			sl.setServiceId(null);
			sl.setProductName(p.getName());
			sl.setQuantity(qty);
			sl.setUnitPrice(unit);
			sale.getLines().add(sl);
		}

		for (Map.Entry<Long, Integer> e : mergedServiceQty.entrySet()) {
			ServiceItem s = serviceItemRepository.findByIdAndBusinessId(e.getKey(), businessId).orElseThrow(
					() -> new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND, HttpStatus.BAD_REQUEST));
			int qty = e.getValue();
			double unit = s.getPrice() != null ? s.getPrice() : 0d;
			double lineTotal = unit * qty;
			total += lineTotal;

			CustomerSaleLine sl = new CustomerSaleLine();
			sl.setSale(sale);
			sl.setBusinessId(businessId);
			sl.setLineType(LINE_SERVICE);
			sl.setProductId(null);
			sl.setServiceId(s.getId());
			sl.setProductName(s.getName());
			sl.setQuantity(qty);
			sl.setUnitPrice(unit);
			sale.getLines().add(sl);
		}

		sale.setTotalAmount(total);
		saleRepository.save(sale);

		// Decrement stock only after the sale row has persisted — keeps the inventory write inside
		// the same transaction so a downstream failure rolls everything back together.
		for (Map.Entry<Long, Integer> e : mergedProductQty.entrySet()) {
			Product p = productRepository.findByIdAndBusinessId(e.getKey(), businessId).orElseThrow(
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
		if (request.getId() == null) {
			throw new ServiceException(ApiMessages.SALE_ID_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		Long businessId = BusinessContextHolder.requireBusinessId();
		CustomerSale sale = saleRepository.findByIdAndBusinessId(request.getId(), businessId)
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
		r.setLineType(l.getLineType());
		r.setProductId(l.getProductId());
		r.setServiceId(l.getServiceId());
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
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize(),
				Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<CustomerSale> page;
		if (request.getCustomerId() != null) {
			page = saleRepository.findByBusinessIdAndCustomerIdOrderByCreatedAtDesc(businessId, request.getCustomerId(),
					pageable);
		} else {
			page = saleRepository.findAllByBusinessId(businessId, pageable);
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

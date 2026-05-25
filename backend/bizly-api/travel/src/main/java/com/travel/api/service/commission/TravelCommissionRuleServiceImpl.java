package com.travel.api.service.commission;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.commission.TravelCommissionRuleDtos.*;
import com.travel.api.model.TravelCommissionRule;
import com.travel.api.repository.TravelCommissionRuleRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelCommissionRuleServiceImpl implements ITravelCommissionRuleService {

	@Autowired
	private TravelCommissionRuleRepository repository;

	@Override
	public AddTravelCommissionRuleResponse add(AddTravelCommissionRuleRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelCommissionRule e = new TravelCommissionRule();
		e.setBusinessId(businessId);
		e.setName(request.getName());
		e.setRuleType(request.getRuleType());
		e.setRatePercent(request.getRatePercent());
		e.setFlatAmount(request.getFlatAmount());
		e.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
		e.setCreatedAt(LocalDateTime.now());
		repository.save(e);
		AddTravelCommissionRuleResponse res = new AddTravelCommissionRuleResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelCommissionRuleResponse update(UpdateTravelCommissionRuleRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelCommissionRule e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.COMMISSION_RULE_NOT_FOUND, HttpStatus.NOT_FOUND));
		e.setName(request.getName());
		e.setRuleType(request.getRuleType());
		e.setRatePercent(request.getRatePercent());
		e.setFlatAmount(request.getFlatAmount());
		if (request.getActive() != null) {
			e.setActive(request.getActive());
		}
		repository.save(e);
		UpdateTravelCommissionRuleResponse res = new UpdateTravelCommissionRuleResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelCommissionRuleResponse delete(DeleteTravelCommissionRuleRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelCommissionRule e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.COMMISSION_RULE_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelCommissionRuleResponse res = new DeleteTravelCommissionRuleResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelCommissionRuleResponse get(GetTravelCommissionRuleRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelCommissionRule e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.COMMISSION_RULE_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelCommissionRuleResponse> gets(GetsTravelCommissionRulesRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelCommissionRule> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelCommissionRuleResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelCommissionRuleResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	private GetTravelCommissionRuleResponse toResponse(TravelCommissionRule e) {
		GetTravelCommissionRuleResponse r = new GetTravelCommissionRuleResponse();
		r.setId(e.getId());
		r.setName(e.getName());
		r.setRuleType(e.getRuleType());
		r.setRatePercent(e.getRatePercent());
		r.setFlatAmount(e.getFlatAmount());
		r.setActive(e.getActive());
		return r;
	}
}

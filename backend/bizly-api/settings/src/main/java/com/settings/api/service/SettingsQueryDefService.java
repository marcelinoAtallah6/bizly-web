package com.settings.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.dto.query.QueryDefGetsRequest;
import com.settings.api.dto.query.QueryDefIdRequest;
import com.settings.api.dto.query.QueryDefResponse;
import com.settings.api.dto.query.QueryDefSaveRequest;
import com.settings.api.model.SettingsQueryDef;
import com.settings.api.repository.SettingsQueryDefRepository;
import com.settings.common.ApiMessages;
import com.settings.common.PageResponse;
import com.settings.exception.ServiceException;

@Service
public class SettingsQueryDefService {

	@Autowired
	private SettingsQueryDefRepository queryDefRepository;

	@Autowired
	private SqlQueryValidationService validationService;

	public PageResponse<QueryDefResponse> listPage(QueryDefGetsRequest req) {
		Pageable pageable = org.springframework.data.domain.PageRequest.of(req.getPageNumber(), req.getPageSize(),
				Sort.by(Sort.Direction.ASC, "name"));
		String fragment = req.getNameSearch() == null ? "" : req.getNameSearch().trim();
		Page<SettingsQueryDef> page = fragment.isEmpty()
				? queryDefRepository.findAll(pageable)
				: queryDefRepository.findByNameContainingIgnoreCase(fragment, pageable);
		List<QueryDefResponse> items = page.getContent().stream().map(this::toResponse).collect(Collectors.toList());
		PageResponse<QueryDefResponse> out = new PageResponse<>();
		out.setItems(items);
		out.setTotalCount(page.getTotalElements());
		out.setPageNumber(page.getNumber());
		out.setPageSize(page.getSize());
		out.setTotalPages(page.getTotalPages());
		return out;
	}

	public QueryDefResponse get(QueryDefIdRequest req) {
		SettingsQueryDef q = queryDefRepository.findById(req.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(q);
	}

	@Transactional
	public QueryDefResponse save(QueryDefSaveRequest req, String username) {
		validationService.validateSelectOnly(req.getSqlText());
		SettingsQueryDef entity;
		if (req.getId() != null) {
			entity = queryDefRepository.findById(req.getId())
					.orElseThrow(() -> new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND));
		} else {
			entity = new SettingsQueryDef();
			if (queryDefRepository.existsByNameIgnoreCase(req.getName())) {
				throw new ServiceException(ApiMessages.SETTINGS_QUERY_NAME_EXISTS, HttpStatus.CONFLICT);
			}
		}
		entity.setName(req.getName().trim());
		entity.setDescription(req.getDescription());
		entity.setSqlText(req.getSqlText());
		entity.setParametersJson(req.getParametersJson());
		if (entity.getCreatedBy() == null && username != null) {
			entity.setCreatedBy(username);
		}
		entity = queryDefRepository.save(entity);
		return toResponse(entity);
	}

	@Transactional
	public void delete(QueryDefIdRequest req) {
		if (!queryDefRepository.existsById(req.getId())) {
			throw new ServiceException(ApiMessages.SETTINGS_QUERY_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		queryDefRepository.deleteById(req.getId());
	}

	private QueryDefResponse toResponse(SettingsQueryDef q) {
		QueryDefResponse r = new QueryDefResponse();
		r.setId(q.getId());
		r.setName(q.getName());
		r.setDescription(q.getDescription());
		r.setSqlText(q.getSqlText());
		r.setParametersJson(q.getParametersJson());
		r.setCreatedAt(q.getCreatedAt());
		r.setUpdatedAt(q.getUpdatedAt());
		r.setCreatedBy(q.getCreatedBy());
		return r;
	}
}

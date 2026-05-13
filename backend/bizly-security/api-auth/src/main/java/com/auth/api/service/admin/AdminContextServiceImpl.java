package com.auth.api.service.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.auth.api.controllers.dto.admin.AdminBusinessDto;
import com.auth.api.controllers.dto.admin.AdminUserDto;
import com.auth.api.model.business.BusinessEntity;
import com.auth.api.model.user.UserEntity;
import com.auth.api.repository.business.BusinessRepository;
import com.auth.api.repository.user.UserRepository;

@Service
public class AdminContextServiceImpl implements IAdminContextService {

	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 50;

	@Autowired private BusinessRepository businessRepository;
	@Autowired private UserRepository userRepository;

	@Override
	public List<AdminBusinessDto> searchBusinesses(String q, Integer limit) {
		Pageable page = PageRequest.of(0, clampLimit(limit));
		String needle = q == null ? "" : q.trim();
		// Accept an exact numeric id as a power-user shortcut.
		try {
			long asId = Long.parseLong(needle);
			List<AdminBusinessDto> direct = new ArrayList<>();
			businessRepository.findById(asId).ifPresent(b -> direct.add(toDto(b)));
			if (!direct.isEmpty()) return direct;
		} catch (NumberFormatException ignore) { /* not numeric */ }
		List<BusinessEntity> found = businessRepository.searchByName(needle, page);
		List<AdminBusinessDto> out = new ArrayList<>(found.size());
		for (BusinessEntity b : found) out.add(toDto(b));
		return out;
	}

	@Override
	public List<AdminUserDto> searchUsers(String q, Integer limit) {
		Pageable page = PageRequest.of(0, clampLimit(limit));
		String needle = q == null ? "" : q.trim();
		List<UserEntity> found = userRepository.searchByUsernameOrEmail(needle, page);
		List<AdminUserDto> out = new ArrayList<>(found.size());
		for (UserEntity u : found) {
			out.add(new AdminUserDto(u.getId(), u.getUsername(), u.getEmail(),
					u.getFirstName(), u.getLastName(), u.getBusinessId()));
		}
		return out;
	}

	private static int clampLimit(Integer requested) {
		if (requested == null || requested <= 0) return DEFAULT_LIMIT;
		return Math.min(requested, MAX_LIMIT);
	}

	private static AdminBusinessDto toDto(BusinessEntity b) {
		return new AdminBusinessDto(b.getId(), b.getBusinessName(), b.getBusinessType(),
				b.getStatus(), b.getCreatedAt());
	}
}

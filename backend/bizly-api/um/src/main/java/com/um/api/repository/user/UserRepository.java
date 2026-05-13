package com.um.api.repository.user;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.um.api.model.user.User;

/**
 * Tenant-scoped finders. Use these from services; never call inherited
 * {@code findById} / {@code findAll} directly — they bypass {@code business_id}.
 * See {@link com.um.security.BusinessContextHolder}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByIdAndBusinessId(Long id, Long businessId);

	Page<User> findAllByBusinessId(Long businessId, Pageable pageable);
}

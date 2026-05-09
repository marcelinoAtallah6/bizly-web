package com.auth.api.service.user;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.auth.api.model.user.UserEntity;
import com.auth.api.repository.user.UserRepository;
import com.auth.api.repository.user.UserRoleRepository;

@Service
public class CustomUserDetailService implements UserDetailsService {

	private static final Logger log = LogManager.getLogger(CustomUserDetailService.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		log.info("Attempting to load user: {}", username);

		UserEntity userEntity = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return new User(userEntity.getUsername(), userEntity.getPassword(), getUserAuthorities(userEntity.getId()));
	}

	private List<SimpleGrantedAuthority> getUserAuthorities(Long userId) {
		// Use join query so orphaned um_user_role rows (invalid role_id) never lazy-load missing RoleEntity.
		return userRoleRepository.findRoleNamesByUserId(userId).stream().map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
	}
}
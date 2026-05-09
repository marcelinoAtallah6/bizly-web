package com.um.api.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.um.api.model.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
}

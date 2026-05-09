package com.um.api.repository.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.model.role.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
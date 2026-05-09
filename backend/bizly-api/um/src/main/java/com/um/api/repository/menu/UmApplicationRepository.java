package com.um.api.repository.menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.model.menu.UmApplication;

@Repository
public interface UmApplicationRepository extends JpaRepository<UmApplication, Long> {

	List<UmApplication> findByIsActiveOrderByNameAsc(Boolean isActive);
}

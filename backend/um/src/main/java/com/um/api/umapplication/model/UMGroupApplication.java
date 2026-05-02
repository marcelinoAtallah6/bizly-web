package com.um.api.umapplication.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "um_group_applications", schema = "um")
public class UMGroupApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "um_group_application_seq")
	@SequenceGenerator(name = "um_group_application_seq", sequenceName = "um.s_um_group_applications", allocationSize = 1)
	@Column(name = "id")
	private Long id;

	@JsonManagedReference
	@OneToMany(mappedBy = "groupApplication")
	private List<UMApplication> applications;

	public List<UMApplication> getApplications() {
		return applications;
	}

	public void setApplications(List<UMApplication> applications) {
		this.applications = applications;
	}

	@Column(name = "name", nullable = false)
	private String name;

	@JsonIgnore
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@JsonIgnore
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}

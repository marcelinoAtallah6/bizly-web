package com.bm.api.dto.serviceitem.get;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetServiceItemResponse {

	private Long id;
	private String name;
	private String description;
	private Double price;
	private Integer durationMinutes;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
}

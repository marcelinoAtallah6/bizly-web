package com.pm.api.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.pm.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.PRODUCT_TABLE, schema = DatabaseConstants.SCHEMA)
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private Double price;

	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	private List<ProductItem> productItems;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	private List<KycCustomerOrder> customerOrders;

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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<ProductItem> getProductItems() {
		return productItems;
	}

	public void setProductItems(List<ProductItem> productItems) {
		this.productItems = productItems;
	}

	public List<KycCustomerOrder> getCustomerOrders() {
		return customerOrders;
	}

	public void setCustomerOrders(List<KycCustomerOrder> customerOrders) {
		this.customerOrders = customerOrders;
	}

}
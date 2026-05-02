package com.store.api.storeconfig.model;


import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;


@Data
@Entity
@Table(name = "business_types", schema = "conf")
public class StoreBusinessType {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "s_business_types")
    @SequenceGenerator(name = "s_business_types", sequenceName = "conf.s_business_types", allocationSize = 1)
    private Long id;

    private String name;
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();


}

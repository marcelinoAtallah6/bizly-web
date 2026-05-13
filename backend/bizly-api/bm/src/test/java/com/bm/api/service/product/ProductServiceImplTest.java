package com.bm.api.service.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.bm.api.dto.product.add.AddProductRequest;
import com.bm.api.dto.product.add.AddProductResponse;
import com.bm.api.dto.product.delete.DeleteProductResponse;
import com.bm.api.dto.product.get.GetProductResponse;
import com.bm.api.dto.product.gets.GetsProductsRequest;
import com.bm.api.dto.product.update.UpdateProductRequest;
import com.bm.api.dto.product.update.UpdateProductResponse;
import com.bm.api.model.Product;
import com.bm.api.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    private Product product;

    @BeforeEach
    public void setup() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(99.99);
    }

    @Test
    public void testAddProduct() {
        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        AddProductRequest request = new AddProductRequest();
        request.setName("Test Product");
        request.setPrice(99.99);

        AddProductResponse addResponse = service.add(request);
        assertNotNull(addResponse);
    }

    @Test
    public void testUpdateProduct() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenReturn(product);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setId(1L);
        request.setName("Updated Product");
        request.setPrice(119.99);

        UpdateProductResponse updateResponse = service.update(request);
        assertNotNull(updateResponse);
    }

    @Test
    public void testDeleteProduct() {
        doNothing().when(repository).deleteById(anyLong());

        com.bm.api.dto.product.delete.DeleteProductRequest request = new com.bm.api.dto.product.delete.DeleteProductRequest();
        request.setId(1L);

        DeleteProductResponse deleteResponse = service.delete(request);
        assertNotNull(deleteResponse);
    }

    @Test
    public void testGetProduct() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(product));

        com.bm.api.dto.product.get.GetProductRequest request = new com.bm.api.dto.product.get.GetProductRequest();
        request.setId(1L);

        GetProductResponse response = service.get(request);
        assertNotNull(response);
        assertEquals("Test Product", response.getName());
    }

    @Test
    public void testGetsProducts() {
        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        GetsProductsRequest request = new GetsProductsRequest();
        request.setPageNumber(0);
        request.setPageSize(10);

        assertNotNull(service.gets(request));
        assertEquals(1, service.gets(request).getItems().size());
    }
}

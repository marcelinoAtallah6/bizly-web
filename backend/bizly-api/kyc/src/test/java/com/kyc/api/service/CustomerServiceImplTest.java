package com.kyc.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.kyc.api.dto.add.AddCustomerRequest;
import com.kyc.api.dto.get.GetCustomerResponse;
import com.kyc.api.dto.gets.GetsCustomersRequest;
import com.kyc.api.dto.update.UpdateCustomerRequest;
import com.kyc.api.model.customer.KycCustomer;
import com.kyc.api.repository.KycCustomerRepository;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private KycCustomerRepository repository;

    @InjectMocks
    private CustomerServiceImpl service;

    private KycCustomer customer;

    @BeforeEach
    public void setup() {
        customer = new KycCustomer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setFullName("John Doe");
        customer.setDob(LocalDate.of(1990, 1, 1));
        customer.setEmail("john.doe@example.com");
        customer.setMobileNumber("+1234567890");
        customer.setCreatedAt(LocalDateTime.now());
    }

    @Test
    public void testAddCustomer() {
        when(repository.save(any(KycCustomer.class))).thenAnswer(invocation -> {
            KycCustomer entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        AddCustomerRequest request = new AddCustomerRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDob(LocalDate.of(1990, 1, 1));
        request.setEmail("john.doe@example.com");
        request.setMobileNumber("+1234567890");
        request.setCustomerStatus("ACTIVE");

        var addResponse = service.add(request);
        assertNotNull(addResponse);
        assertEquals(1L, addResponse.getId());
    }

    @Test
    public void testUpdateCustomer() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(repository.save(any(KycCustomer.class))).thenReturn(customer);

        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setId(1L);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDob(LocalDate.of(1990, 1, 1));
        request.setEmail("john.doe@example.com");
        request.setMobileNumber("+1234567890");
        request.setCustomerStatus("ACTIVE");

        var updateResponse = service.update(request);
        assertNotNull(updateResponse);
        assertEquals(1L, updateResponse.getId());
    }

    @Test
    public void testDeleteCustomer() {
        when(repository.existsById(anyLong())).thenReturn(true);
        doNothing().when(repository).deleteById(anyLong());

        com.kyc.api.dto.delete.DeleteCustomerRequest request = new com.kyc.api.dto.delete.DeleteCustomerRequest();
        request.setId(1L);

        var deleteResponse = service.delete(request);
        assertNotNull(deleteResponse);
        assertEquals(1L, deleteResponse.getId());
    }

    @Test
    public void testGetCustomer() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(customer));

        com.kyc.api.dto.get.GetCustomerRequest request = new com.kyc.api.dto.get.GetCustomerRequest();
        request.setId(1L);

        GetCustomerResponse response = service.get(request);
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
    }

    @Test
    public void testGetsCustomers() {
        Page<KycCustomer> page = new PageImpl<>(List.of(customer), PageRequest.of(0, 10), 1);
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        GetsCustomersRequest request = new GetsCustomersRequest();
        request.setPageNumber(0);
        request.setPageSize(10);

        assertNotNull(service.gets(request));
        assertEquals(1, service.gets(request).getItems().size());
    }
}

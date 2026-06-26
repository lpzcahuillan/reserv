package com.lpzcahuillan.customer.service;

import com.lpzcahuillan.customer.dto.CustomerRequest;
import com.lpzcahuillan.customer.dto.CustomerResponse;
import com.lpzcahuillan.customer.entity.Customer;
import com.lpzcahuillan.customer.exception.BadRequestException;
import com.lpzcahuillan.customer.exception.ResourceNotFoundException;
import com.lpzcahuillan.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerServiceImpl service;

    @Test
    void createCustomer_Success() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+123456789")
                .build();

        Customer savedCustomer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+123456789")
                .build();

        when(repository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(repository.save(any(Customer.class))).thenReturn(savedCustomer);

        // When
        CustomerResponse response = service.createCustomer(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("john.doe@example.com", response.getEmail());
        verify(repository).save(any(Customer.class));
    }

    @Test
    void createCustomer_EmailAlreadyExists() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+123456789")
                .build();

        when(repository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.createCustomer(request));
        assertEquals("Email already exists", exception.getMessage());
        verify(repository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_Success() {
        // Given
        Long customerId = 1L;
        Customer customer = Customer.builder()
                .id(customerId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+123456789")
                .build();

        when(repository.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        CustomerResponse response = service.getCustomerById(customerId);

        // Then
        assertNotNull(response);
        assertEquals(customerId, response.getId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    void getCustomerById_NotFound() {
        // Given
        Long customerId = 99L;
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.getCustomerById(customerId));
        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void getAllCustomers_Success() {
        // Given
        Customer customer1 = Customer.builder().id(1L).firstName("John").lastName("Doe").email("john@example.com").build();
        Customer customer2 = Customer.builder().id(2L).firstName("Jane").lastName("Doe").email("jane@example.com").build();
        when(repository.findAll()).thenReturn(java.util.List.of(customer1, customer2));

        // When
        java.util.List<CustomerResponse> results = service.getAllCustomers();

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("John", results.get(0).getFirstName());
        assertEquals("Jane", results.get(1).getFirstName());
    }

    @Test
    void updateCustomer_Success() {
        // Given
        Long customerId = 1L;
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John Updated")
                .lastName("Doe")
                .email("john.updated@example.com")
                .phone("+123456789")
                .build();

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+123456789")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .firstName("John Updated")
                .lastName("Doe")
                .email("john.updated@example.com")
                .phone("+123456789")
                .build();

        when(repository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(repository.existsByEmail("john.updated@example.com")).thenReturn(false);
        when(repository.save(any(Customer.class))).thenReturn(updatedCustomer);

        // When
        CustomerResponse response = service.updateCustomer(customerId, request);

        // Then
        assertNotNull(response);
        assertEquals("John Updated", response.getFirstName());
        assertEquals("john.updated@example.com", response.getEmail());
        verify(repository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_EmailSame_Success() {
        // Given
        Long customerId = 1L;
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John Updated")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+123456789")
                .build();

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+123456789")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(customerId)
                .firstName("John Updated")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+123456789")
                .build();

        when(repository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        // existsByEmail should not be called since email is same
        when(repository.save(any(Customer.class))).thenReturn(updatedCustomer);

        // When
        CustomerResponse response = service.updateCustomer(customerId, request);

        // Then
        assertNotNull(response);
        assertEquals("John Updated", response.getFirstName());
        assertEquals("john@example.com", response.getEmail());
    }

    @Test
    void updateCustomer_NotFound() {
        // Given
        Long customerId = 99L;
        CustomerRequest request = CustomerRequest.builder().firstName("John").build();
        when(repository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.updateCustomer(customerId, request));
        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void updateCustomer_EmailAlreadyExists() {
        // Given
        Long customerId = 1L;
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("other@example.com")
                .build();

        Customer existingCustomer = Customer.builder()
                .id(customerId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        when(repository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(repository.existsByEmail("other@example.com")).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.updateCustomer(customerId, request));
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void deleteCustomer_Success() {
        // Given
        Long customerId = 1L;
        when(repository.existsById(customerId)).thenReturn(true);
        doNothing().when(repository).deleteById(customerId);

        // When
        assertDoesNotThrow(() -> service.deleteCustomer(customerId));

        // Then
        verify(repository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_NotFound() {
        // Given
        Long customerId = 99L;
        when(repository.existsById(customerId)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.deleteCustomer(customerId));
        assertEquals("Customer not found", exception.getMessage());
        verify(repository, never()).deleteById(customerId);
    }
}

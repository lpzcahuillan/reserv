package com.lpzcahuillan.customer.service;

import com.lpzcahuillan.customer.dto.CustomerRequest;
import com.lpzcahuillan.customer.dto.CustomerResponse;
import com.lpzcahuillan.customer.entity.Customer;
import com.lpzcahuillan.customer.exception.BadRequestException;
import com.lpzcahuillan.customer.exception.ResourceNotFoundException;
import com.lpzcahuillan.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());
        if (repository.existsByEmail(request.getEmail())) {
            log.warn("Attempt to create customer with existing email: {}", request.getEmail());
            throw new BadRequestException("Email already exists");
        }
        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        Customer saved = repository.save(customer);
        log.info("Customer created successfully with id: {} and email: {}", saved.getId(), saved.getEmail());
        return mapToResponse(saved);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        log.debug("Fetching customer with id: {}", id);
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id: {}", id);
                    return new ResourceNotFoundException("Customer not found");
                });
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        log.debug("Fetching all customers");
        List<CustomerResponse> customers = repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Retrieved {} customers", customers.size());
        return customers;
    }

    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("Updating customer with id: {}", id);
        Customer customer = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id: {} during update", id);
                    return new ResourceNotFoundException("Customer not found");
                });

        if (!customer.getEmail().equals(request.getEmail()) && repository.existsByEmail(request.getEmail())) {
            log.warn("Attempt to update customer {} with existing email: {}", id, request.getEmail());
            throw new BadRequestException("Email already exists");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        Customer updated = repository.save(customer);
        log.info("Customer with id: {} updated successfully", id);
        return mapToResponse(updated);
    }

    @Override
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with id: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Customer not found with id: {} during delete", id);
            throw new ResourceNotFoundException("Customer not found");
        }
        repository.deleteById(id);
        log.info("Customer with id: {} deleted successfully", id);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .build();
    }
}

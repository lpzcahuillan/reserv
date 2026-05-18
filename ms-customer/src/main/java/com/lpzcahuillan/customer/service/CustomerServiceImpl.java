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
        log.info("Creando cliente con email: {}", request.getEmail());
        if (repository.existsByEmail(request.getEmail())) {
            log.warn("Intento de crear cliente con email existente: {}", request.getEmail());
            throw new BadRequestException("Email already exists");
        }
        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        Customer saved = repository.save(customer);
        log.info("Cliente creado exitosamente con id: {} y email: {}", saved.getId(), saved.getEmail());
        return mapToResponse(saved);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        log.debug("Obteniendo cliente con id: {}", id);
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado con id: {}", id);
                    return new ResourceNotFoundException("Customer not found");
                });
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        log.debug("Obteniendo todos los clientes");
        List<CustomerResponse> customers = repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Se obtuvieron {} clientes", customers.size());
        return customers;
    }

    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("Actualizando cliente con id: {}", id);
        Customer customer = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado con id: {} durante la actualización", id);
                    return new ResourceNotFoundException("Customer not found");
                });

        if (!customer.getEmail().equals(request.getEmail()) && repository.existsByEmail(request.getEmail())) {
            log.warn("Intento de actualizar cliente {} con email existente: {}", id, request.getEmail());
            throw new BadRequestException("Email already exists");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        Customer updated = repository.save(customer);
        log.info("Cliente con id: {} actualizado exitosamente", id);
        return mapToResponse(updated);
    }

    @Override
    public void deleteCustomer(Long id) {
        log.info("Eliminando cliente con id: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Cliente no encontrado con id: {} durante la eliminación", id);
            throw new ResourceNotFoundException("Customer not found");
        }
        repository.deleteById(id);
        log.info("Cliente con id: {} eliminado exitosamente", id);
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

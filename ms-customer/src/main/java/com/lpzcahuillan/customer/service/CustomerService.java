package com.lpzcahuillan.customer.service;

import com.lpzcahuillan.customer.dto.CustomerRequest;
import com.lpzcahuillan.customer.dto.CustomerResponse;
import java.util.List;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerRequest request);
    CustomerResponse getCustomerById(Long id);
    List<CustomerResponse> getAllCustomers();
    CustomerResponse updateCustomer(Long id, CustomerRequest request);
    void deleteCustomer(Long id);
}

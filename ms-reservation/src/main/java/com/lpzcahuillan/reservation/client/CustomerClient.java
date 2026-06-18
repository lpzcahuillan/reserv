package com.lpzcahuillan.reservation.client;

import com.lpzcahuillan.reservation.dto.CustomerDTO;
import com.lpzcahuillan.reservation.client.fallback.CustomerClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-customer", fallback = CustomerClientFallback.class)
public interface CustomerClient {
    @GetMapping("/api/customers/{id}")
    CustomerDTO getCustomerById(@PathVariable("id") Long id);
}

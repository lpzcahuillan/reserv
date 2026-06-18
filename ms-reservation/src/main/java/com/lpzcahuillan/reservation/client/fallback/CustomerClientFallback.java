package com.lpzcahuillan.reservation.client.fallback;

import com.lpzcahuillan.reservation.client.CustomerClient;
import com.lpzcahuillan.reservation.dto.CustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerClientFallback implements CustomerClient {
    @Override
    public CustomerDTO getCustomerById(Long id) {
        log.warn("Servicio ms-customer no disponible. Retornando cliente temporal para id: {}", id);
        CustomerDTO fallbackCustomer = new CustomerDTO();
        fallbackCustomer.setId(id);
        fallbackCustomer.setFirstName("Usuario");
        fallbackCustomer.setLastName("Temporal (Fallback)");
        fallbackCustomer.setEmail("fallback@restaurant.com");
        return fallbackCustomer;
    }
}

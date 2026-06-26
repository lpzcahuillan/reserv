package com.lpzcahuillan.reservation.client.fallback;

import com.lpzcahuillan.reservation.client.CustomerClient;
import com.lpzcahuillan.reservation.dto.CustomerDTO;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerClientFallbackFactory implements FallbackFactory<CustomerClient> {

    @Override
    public CustomerClient create(Throwable cause) {
        return new CustomerClient() {
            @Override
            public CustomerDTO getCustomerById(Long id) {
                if (cause instanceof FeignException && ((FeignException) cause).status() == 404) {
                    log.warn("Cliente con ID {} no existe en ms-customer (404)", id);
                    throw (FeignException) cause;
                }
                log.warn("Servicio ms-customer no disponible. Fallback disparado por: {}", cause.getMessage());
                CustomerDTO fallbackCustomer = new CustomerDTO();
                fallbackCustomer.setId(id);
                fallbackCustomer.setFirstName("Usuario");
                fallbackCustomer.setLastName("Temporal (Fallback)");
                fallbackCustomer.setEmail("fallback@restaurant.com");
                return fallbackCustomer;
            }
        };
    }
}

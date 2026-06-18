package com.lpzcahuillan.order.client.fallback;

import com.lpzcahuillan.order.client.MenuClient;
import com.lpzcahuillan.order.dto.MenuItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class MenuClientFallback implements MenuClient {
    @Override
    public MenuItemDTO getMenuItemById(Long id) {
        log.warn("Servicio ms-menu no disponible. Retornando item temporal para id: {}", id);
        MenuItemDTO fallbackItem = new MenuItemDTO();
        fallbackItem.setId(id);
        fallbackItem.setName("Platillo Temporal (Fallback)");
        fallbackItem.setPrice(BigDecimal.ZERO);
        fallbackItem.setStatus("AVAILABLE");
        return fallbackItem;
    }
}

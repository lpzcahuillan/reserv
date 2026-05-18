package com.lpzcahuillan.order.client;

import com.lpzcahuillan.order.dto.MenuItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-menu")
public interface MenuClient {
    @GetMapping("/api/menu/items/{id}")
    MenuItemDTO getMenuItemById(@PathVariable("id") Long id);
}

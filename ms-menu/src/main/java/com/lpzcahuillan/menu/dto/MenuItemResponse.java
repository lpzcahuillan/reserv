package com.lpzcahuillan.menu.dto;

import com.lpzcahuillan.menu.entity.MenuItem.ItemStatus;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private ItemStatus status;
    private Long categoryId;
}

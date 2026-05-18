package com.lpzcahuillan.table.dto;

import com.lpzcahuillan.table.entity.RestaurantTable.TableStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableRequest {
    private String tableNumber;
    private Integer capacity;
    private TableStatus status;
}

package com.lpzcahuillan.table.dto;

import com.lpzcahuillan.table.entity.RestaurantTable.TableStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableResponse {
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private TableStatus status;
}

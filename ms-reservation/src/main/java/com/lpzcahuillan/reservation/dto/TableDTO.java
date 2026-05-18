package com.lpzcahuillan.reservation.dto;

import lombok.Data;

@Data
public class TableDTO {
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private String status;
}

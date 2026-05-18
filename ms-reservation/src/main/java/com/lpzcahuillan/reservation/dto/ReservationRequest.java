package com.lpzcahuillan.reservation.dto;

import com.lpzcahuillan.reservation.entity.Reservation.ReservationStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    private Long customerId;
    private Long tableId;
    private LocalDateTime reservationTime;
    private Integer numberOfPeople;
    private ReservationStatus status;
}

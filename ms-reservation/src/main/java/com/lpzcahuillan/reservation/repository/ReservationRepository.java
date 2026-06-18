package com.lpzcahuillan.reservation.repository;

import com.lpzcahuillan.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomerId(Long customerId);
    List<Reservation> findByTableId(Long tableId);
    boolean existsByTableIdAndReservationTimeAndStatusNot(Long tableId, java.time.LocalDateTime reservationTime, Reservation.ReservationStatus status);
    boolean existsByTableIdAndReservationTimeAndStatusNotAndIdNot(Long tableId, java.time.LocalDateTime reservationTime, Reservation.ReservationStatus status, Long id);
}

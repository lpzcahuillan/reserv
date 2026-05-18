package com.lpzcahuillan.reservation.service;

import com.lpzcahuillan.reservation.dto.ReservationRequest;
import com.lpzcahuillan.reservation.dto.ReservationResponse;
import java.util.List;

public interface ReservationService {
    ReservationResponse createReservation(ReservationRequest request);
    ReservationResponse getReservationById(Long id);
    List<ReservationResponse> getAllReservations();
    ReservationResponse updateReservation(Long id, ReservationRequest request);
    void deleteReservation(Long id);
}

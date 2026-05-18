package com.lpzcahuillan.reservation.service;

import com.lpzcahuillan.reservation.client.CustomerClient;
import com.lpzcahuillan.reservation.client.TableClient;
import com.lpzcahuillan.reservation.dto.ReservationRequest;
import com.lpzcahuillan.reservation.dto.ReservationResponse;
import com.lpzcahuillan.reservation.entity.Reservation;
import com.lpzcahuillan.reservation.exception.BadRequestException;
import com.lpzcahuillan.reservation.exception.ResourceNotFoundException;
import com.lpzcahuillan.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository repository;
    private final CustomerClient customerClient;
    private final TableClient tableClient;

    @Override
    public ReservationResponse createReservation(ReservationRequest request) {
        validateCustomerAndTable(request.getCustomerId(), request.getTableId());

        Reservation reservation = Reservation.builder()
                .customerId(request.getCustomerId())
                .tableId(request.getTableId())
                .reservationTime(request.getReservationTime())
                .numberOfPeople(request.getNumberOfPeople())
                .status(request.getStatus() != null ? request.getStatus() : Reservation.ReservationStatus.PENDING)
                .build();
        return mapToResponse(repository.save(reservation));
    }

    @Override
    public ReservationResponse getReservationById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
    }

    @Override
    public List<ReservationResponse> getAllReservations() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationResponse updateReservation(Long id, ReservationRequest request) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        validateCustomerAndTable(request.getCustomerId(), request.getTableId());

        reservation.setCustomerId(request.getCustomerId());
        reservation.setTableId(request.getTableId());
        reservation.setReservationTime(request.getReservationTime());
        reservation.setNumberOfPeople(request.getNumberOfPeople());
        reservation.setStatus(request.getStatus());

        return mapToResponse(repository.save(reservation));
    }

    @Override
    public void deleteReservation(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation not found");
        }
        repository.deleteById(id);
    }

    private void validateCustomerAndTable(Long customerId, Long tableId) {
        try {
            customerClient.getCustomerById(customerId);
        } catch (Exception e) {
            throw new BadRequestException("Customer with ID " + customerId + " does not exist");
        }

        try {
            tableClient.getTableById(tableId);
        } catch (Exception e) {
            throw new BadRequestException("Table with ID " + tableId + " does not exist");
        }
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .customerId(reservation.getCustomerId())
                .tableId(reservation.getTableId())
                .reservationTime(reservation.getReservationTime())
                .numberOfPeople(reservation.getNumberOfPeople())
                .status(reservation.getStatus())
                .build();
    }
}

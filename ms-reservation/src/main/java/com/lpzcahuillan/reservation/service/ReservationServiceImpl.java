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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository repository;
    private final CustomerClient customerClient;
    private final TableClient tableClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public ReservationResponse createReservation(ReservationRequest request) {
        log.info("Creando reserva para cliente: {}, mesa: {}", request.getCustomerId(), request.getTableId());
        validateCustomerAndTable(request.getCustomerId(), request.getTableId());

        boolean tableBusy = repository.existsByTableIdAndReservationTimeAndStatusNot(
                request.getTableId(),
                request.getReservationTime(),
                Reservation.ReservationStatus.CANCELLED
        );
        if (tableBusy) {
            log.warn("La mesa con ID {} ya tiene una reserva activa para el horario {}", request.getTableId(), request.getReservationTime());
            throw new BadRequestException("La mesa ya se encuentra reservada para la fecha y hora seleccionada.");
        }

        Reservation reservation = Reservation.builder()
                .customerId(request.getCustomerId())
                .tableId(request.getTableId())
                .reservationTime(request.getReservationTime())
                .numberOfPeople(request.getNumberOfPeople())
                .status(request.getStatus() != null ? request.getStatus() : Reservation.ReservationStatus.PENDING)
                .build();
        Reservation saved = repository.save(reservation);
        log.info("Reserva creada exitosamente con id: {}", saved.getId());

        // Publicar evento en RabbitMQ
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "RESERVATION_CREATED");
            event.put("message", "¡Nueva reserva registrada exitosamente!");
            event.put("details", String.format("Reserva ID: %d, Cliente ID: %d, Mesa ID: %d, Hora: %s, Personas: %d",
                    saved.getId(), saved.getCustomerId(), saved.getTableId(), saved.getReservationTime(), saved.getNumberOfPeople()));
            rabbitTemplate.convertAndSend("notification.queue", event);
            log.info("Evento RESERVATION_CREATED publicado en RabbitMQ");
        } catch (Exception e) {
            log.error("Fallo al publicar evento RESERVATION_CREATED en RabbitMQ", e);
        }

        return mapToResponse(saved);
    }

    @Override
    public ReservationResponse getReservationById(Long id) {
        log.debug("Obteniendo reserva con id: {}", id);
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.warn("Reserva no encontrada con id: {}", id);
                    return new ResourceNotFoundException("Reservation not found");
                });
    }

    @Override
    public List<ReservationResponse> getAllReservations() {
        log.debug("Obteniendo todas las reservas");
        List<ReservationResponse> reservations = repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Se obtuvieron {} reservas", reservations.size());
        return reservations;
    }

    @Override
    public ReservationResponse updateReservation(Long id, ReservationRequest request) {
        log.info("Actualizando reserva con id: {}", id);
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reserva no encontrada con id: {} durante la actualización", id);
                    return new ResourceNotFoundException("Reservation not found");
                });

        validateCustomerAndTable(request.getCustomerId(), request.getTableId());

        if (request.getStatus() != Reservation.ReservationStatus.CANCELLED) {
            boolean tableBusy = repository.existsByTableIdAndReservationTimeAndStatusNotAndIdNot(
                    request.getTableId(),
                    request.getReservationTime(),
                    Reservation.ReservationStatus.CANCELLED,
                    id
            );
            if (tableBusy) {
                log.warn("La mesa con ID {} ya tiene una reserva activa para el horario {}", request.getTableId(), request.getReservationTime());
                throw new BadRequestException("La mesa ya se encuentra reservada para la fecha y hora seleccionada.");
            }
        }

        reservation.setCustomerId(request.getCustomerId());
        reservation.setTableId(request.getTableId());
        reservation.setReservationTime(request.getReservationTime());
        reservation.setNumberOfPeople(request.getNumberOfPeople());
        reservation.setStatus(request.getStatus());

        repository.save(reservation);
        log.info("Reserva con id: {} actualizada exitosamente", id);
        return mapToResponse(reservation);
    }

    @Override
    public void deleteReservation(Long id) {
        log.info("Eliminando reserva con id: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Reserva no encontrada con id: {} durante la eliminación", id);
            throw new ResourceNotFoundException("Reservation not found");
        }
        repository.deleteById(id);
        log.info("Reserva con id: {} eliminada exitosamente", id);
    }

    private void validateCustomerAndTable(Long customerId, Long tableId) {
        try {
            log.debug("Validando cliente con id: {}", customerId);
            customerClient.getCustomerById(customerId);
        } catch (Exception e) {
            log.warn("Cliente con ID {} no existe", customerId);
            throw new BadRequestException("Customer with ID " + customerId + " does not exist");
        }

        try {
            log.debug("Validando mesa con id: {}", tableId);
            tableClient.getTableById(tableId);
        } catch (Exception e) {
            log.warn("Mesa con ID {} no existe", tableId);
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

package com.lpzcahuillan.reservation.service;

import com.lpzcahuillan.reservation.client.CustomerClient;
import com.lpzcahuillan.reservation.client.TableClient;
import com.lpzcahuillan.reservation.dto.ReservationRequest;
import com.lpzcahuillan.reservation.dto.ReservationResponse;
import com.lpzcahuillan.reservation.entity.Reservation;
import com.lpzcahuillan.reservation.exception.BadRequestException;
import com.lpzcahuillan.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private TableClient tableClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReservationServiceImpl service;

    @Test
    void createReservation_Success() {
        // Given
        LocalDateTime time = LocalDateTime.now();
        ReservationRequest request = ReservationRequest.builder()
                .customerId(1L)
                .tableId(2L)
                .reservationTime(time)
                .numberOfPeople(4)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        Reservation savedReservation = Reservation.builder()
                .id(100L)
                .customerId(1L)
                .tableId(2L)
                .reservationTime(time)
                .numberOfPeople(4)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        when(customerClient.getCustomerById(1L)).thenReturn(null);
        when(tableClient.getTableById(2L)).thenReturn(null);
        when(repository.existsByTableIdAndReservationTimeAndStatusNot(eq(2L), any(), eq(Reservation.ReservationStatus.CANCELLED)))
                .thenReturn(false);
        when(repository.save(any(Reservation.class))).thenReturn(savedReservation);

        // When
        ReservationResponse response = service.createReservation(request);

        // Then
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getCustomerId());
        assertEquals(2L, response.getTableId());
        assertEquals(Reservation.ReservationStatus.PENDING, response.getStatus());

        verify(customerClient).getCustomerById(1L);
        verify(tableClient).getTableById(2L);
        verify(repository).save(any(Reservation.class));
        verify(rabbitTemplate).convertAndSend(eq("notification.queue"), anyMap());
    }

    @Test
    void createReservation_CustomerDoesNotExist() {
        // Given
        ReservationRequest request = ReservationRequest.builder()
                .customerId(99L)
                .tableId(2L)
                .reservationTime(LocalDateTime.now())
                .numberOfPeople(4)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        when(customerClient.getCustomerById(99L)).thenThrow(new RuntimeException("Customer not found"));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.createReservation(request));
        assertEquals("Customer with ID 99 does not exist", exception.getMessage());

        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_TableDoesNotExist() {
        // Given
        ReservationRequest request = ReservationRequest.builder()
                .customerId(1L)
                .tableId(99L)
                .reservationTime(LocalDateTime.now())
                .numberOfPeople(4)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        when(customerClient.getCustomerById(1L)).thenReturn(null);
        when(tableClient.getTableById(99L)).thenThrow(new RuntimeException("Table not found"));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.createReservation(request));
        assertEquals("Table with ID 99 does not exist", exception.getMessage());

        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_TableIsBusy() {
        // Given
        ReservationRequest request = ReservationRequest.builder()
                .customerId(1L)
                .tableId(2L)
                .reservationTime(LocalDateTime.now())
                .numberOfPeople(4)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        when(customerClient.getCustomerById(1L)).thenReturn(null);
        when(tableClient.getTableById(2L)).thenReturn(null);
        when(repository.existsByTableIdAndReservationTimeAndStatusNot(eq(2L), any(), eq(Reservation.ReservationStatus.CANCELLED)))
                .thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.createReservation(request));
        assertEquals("La mesa ya se encuentra reservada para la fecha y hora seleccionada.", exception.getMessage());

        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    void updateReservation_Success() {
        // Given
        Long reservationId = 100L;
        LocalDateTime time = LocalDateTime.now();
        ReservationRequest request = ReservationRequest.builder()
                .customerId(1L)
                .tableId(3L)
                .reservationTime(time)
                .numberOfPeople(4)
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();

        Reservation existingReservation = Reservation.builder()
                .id(reservationId)
                .customerId(1L)
                .tableId(2L)
                .reservationTime(time.minusDays(1))
                .numberOfPeople(2)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        when(repository.findById(reservationId)).thenReturn(Optional.of(existingReservation));
        when(customerClient.getCustomerById(1L)).thenReturn(null);
        when(tableClient.getTableById(3L)).thenReturn(null);
        when(repository.existsByTableIdAndReservationTimeAndStatusNotAndIdNot(eq(3L), any(), eq(Reservation.ReservationStatus.CANCELLED), eq(reservationId)))
                .thenReturn(false);

        // When
        ReservationResponse response = service.updateReservation(reservationId, request);

        // Then
        assertNotNull(response);
        assertEquals(3L, response.getTableId());
        assertEquals(Reservation.ReservationStatus.CONFIRMED, response.getStatus());
        verify(repository).save(existingReservation);
    }

    @Test
    void updateReservation_TableBusyConflict() {
        // Given
        Long reservationId = 100L;
        LocalDateTime time = LocalDateTime.now();
        ReservationRequest request = ReservationRequest.builder()
                .customerId(1L)
                .tableId(3L)
                .reservationTime(time)
                .numberOfPeople(4)
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();

        Reservation existingReservation = Reservation.builder()
                .id(reservationId)
                .customerId(1L)
                .tableId(2L)
                .reservationTime(time.minusDays(1))
                .numberOfPeople(2)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        when(repository.findById(reservationId)).thenReturn(Optional.of(existingReservation));
        when(customerClient.getCustomerById(1L)).thenReturn(null);
        when(tableClient.getTableById(3L)).thenReturn(null);
        when(repository.existsByTableIdAndReservationTimeAndStatusNotAndIdNot(eq(3L), any(), eq(Reservation.ReservationStatus.CANCELLED), eq(reservationId)))
                .thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.updateReservation(reservationId, request));
        assertEquals("La mesa ya se encuentra reservada para la fecha y hora seleccionada.", exception.getMessage());

        verify(repository, never()).save(any(Reservation.class));
    }
}

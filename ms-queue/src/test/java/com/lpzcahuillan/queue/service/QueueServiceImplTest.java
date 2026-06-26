package com.lpzcahuillan.queue.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lpzcahuillan.queue.dto.QueueRequest;
import com.lpzcahuillan.queue.dto.QueueResponse;
import com.lpzcahuillan.queue.entity.QueueEntry;
import com.lpzcahuillan.queue.exception.ResourceNotFoundException;
import com.lpzcahuillan.queue.repository.QueueRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QueueServiceImplTest {

    @Mock
    private QueueRepository repository;

    @InjectMocks
    private QueueServiceImpl service;

    @Test
    void addToQueue_Success() {
        // Given
        QueueRequest request = QueueRequest.builder()
            .customerName("John Doe")
            .partySize(4)
            .build();

        QueueEntry savedEntry = QueueEntry.builder()
            .id(1L)
            .customerName("John Doe")
            .partySize(4)
            .entryTime(LocalDateTime.now())
            .status(QueueEntry.QueueStatus.WAITING)
            .queuePosition(3)
            .build();

        when(
            repository.countByStatus(QueueEntry.QueueStatus.WAITING)
        ).thenReturn(2L);
        when(repository.save(any(QueueEntry.class))).thenReturn(savedEntry);

        // When
        QueueResponse response = service.addToQueue(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals(4, response.getPartySize());
        assertEquals(3, response.getQueuePosition());
        assertEquals(QueueEntry.QueueStatus.WAITING, response.getStatus());

        verify(repository).countByStatus(QueueEntry.QueueStatus.WAITING);
        verify(repository).save(any(QueueEntry.class));
    }

    @Test
    void getNextInQueue_Success() {
        // Given
        QueueEntry first = QueueEntry.builder()
            .id(1L)
            .customerName("John")
            .queuePosition(1)
            .status(QueueEntry.QueueStatus.WAITING)
            .build();
        QueueEntry second = QueueEntry.builder()
            .id(2L)
            .customerName("Jane")
            .queuePosition(2)
            .status(QueueEntry.QueueStatus.WAITING)
            .build();

        when(
            repository.findFirstByStatusOrderByQueuePositionAsc(
                QueueEntry.QueueStatus.WAITING
            )
        ).thenReturn(Optional.of(first));
        when(
            repository.findByStatusOrderByQueuePositionAsc(
                QueueEntry.QueueStatus.WAITING
            )
        ).thenReturn(List.of(second)); // second is left waiting, first becomes CALLED

        // When
        QueueResponse response = service.getNextInQueue();

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(QueueEntry.QueueStatus.CALLED, response.getStatus());
        assertEquals(1, second.getQueuePosition()); // Updated position

        verify(repository).save(first);
        verify(repository).saveAll(List.of(second));
    }

    @Test
    void getNextInQueue_EmptyQueue() {
        // Given
        when(
            repository.findFirstByStatusOrderByQueuePositionAsc(
                QueueEntry.QueueStatus.WAITING
            )
        ).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.getNextInQueue()
        );
        assertEquals("No one is waiting in queue", exception.getMessage());
        verify(repository, never()).save(any(QueueEntry.class));
    }

    @Test
    void getById_Success() {
        // Given
        Long id = 1L;
        QueueEntry entry = QueueEntry.builder()
            .id(id)
            .customerName("John")
            .build();
        when(repository.findById(id)).thenReturn(Optional.of(entry));

        // When
        QueueResponse response = service.getById(id);

        // Then
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("John", response.getCustomerName());
    }

    @Test
    void getById_NotFound() {
        // Given
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.getById(id)
        );
        assertEquals("Queue entry not found", exception.getMessage());
    }

    @Test
    void getWaitingQueue_Success() {
        // Given
        QueueEntry entry1 = QueueEntry.builder()
            .id(1L)
            .queuePosition(1)
            .status(QueueEntry.QueueStatus.WAITING)
            .build();
        QueueEntry entry2 = QueueEntry.builder()
            .id(2L)
            .queuePosition(2)
            .status(QueueEntry.QueueStatus.WAITING)
            .build();
        when(
            repository.findByStatusOrderByQueuePositionAsc(
                QueueEntry.QueueStatus.WAITING
            )
        ).thenReturn(List.of(entry1, entry2));

        // When
        List<QueueResponse> results = service.getWaitingQueue();

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void updateStatus_Success_ToCalled() {
        // Given
        Long id = 1L;
        QueueEntry entry = QueueEntry.builder()
            .id(id)
            .customerName("John")
            .status(QueueEntry.QueueStatus.WAITING)
            .build();
        QueueEntry waiting = QueueEntry.builder()
            .id(2L)
            .customerName("Jane")
            .queuePosition(2)
            .status(QueueEntry.QueueStatus.WAITING)
            .build();

        when(repository.findById(id)).thenReturn(Optional.of(entry));
        when(
            repository.findByStatusOrderByQueuePositionAsc(
                QueueEntry.QueueStatus.WAITING
            )
        ).thenReturn(List.of(waiting));

        // When
        QueueResponse response = service.updateStatus(id, "called");

        // Then
        assertNotNull(response);
        assertEquals(QueueEntry.QueueStatus.CALLED, response.getStatus());
        assertEquals(1, waiting.getQueuePosition()); // Position updated for waiting

        verify(repository).save(entry);
        verify(repository).saveAll(List.of(waiting));
    }

    @Test
    void updateStatus_Success_ToWaiting() {
        // Given
        Long id = 1L;
        QueueEntry entry = QueueEntry.builder()
            .id(id)
            .customerName("John")
            .status(QueueEntry.QueueStatus.CALLED)
            .build();

        when(repository.findById(id)).thenReturn(Optional.of(entry));

        // When
        QueueResponse response = service.updateStatus(id, "waiting");

        // Then
        assertNotNull(response);
        assertEquals(QueueEntry.QueueStatus.WAITING, response.getStatus());

        verify(repository).save(entry);
        verify(repository, never()).saveAll(any());
    }

    @Test
    void updateStatus_NotFound() {
        // Given
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.updateStatus(id, "called")
        );
        assertEquals("Queue entry not found", exception.getMessage());
    }

    @Test
    void updateStatus_InvalidStatus() {
        // Given
        Long id = 1L;
        QueueEntry entry = QueueEntry.builder().id(id).build();
        when(repository.findById(id)).thenReturn(Optional.of(entry));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            service.updateStatus(id, "invalid_status")
        );
    }

    @Test
    void removeFromQueue_Success() {
        // Given
        Long id = 1L;
        QueueEntry waiting = QueueEntry.builder()
            .id(2L)
            .customerName("Jane")
            .queuePosition(2)
            .status(QueueEntry.QueueStatus.WAITING)
            .build();

        when(repository.existsById(id)).thenReturn(true);
        doNothing().when(repository).deleteById(id);
        when(
            repository.findByStatusOrderByQueuePositionAsc(
                QueueEntry.QueueStatus.WAITING
            )
        ).thenReturn(List.of(waiting));

        // When
        assertDoesNotThrow(() -> service.removeFromQueue(id));

        // Then
        verify(repository).deleteById(id);
        assertEquals(1, waiting.getQueuePosition()); // Position updated
        verify(repository).saveAll(List.of(waiting));
    }

    @Test
    void removeFromQueue_NotFound() {
        // Given
        Long id = 99L;
        when(repository.existsById(id)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.removeFromQueue(id)
        );
        assertEquals("Queue entry not found", exception.getMessage());
        verify(repository, never()).deleteById(id);
    }
}

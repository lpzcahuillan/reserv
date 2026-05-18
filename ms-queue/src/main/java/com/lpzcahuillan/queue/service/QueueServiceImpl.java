package com.lpzcahuillan.queue.service;

import com.lpzcahuillan.queue.dto.QueueRequest;
import com.lpzcahuillan.queue.dto.QueueResponse;
import com.lpzcahuillan.queue.entity.QueueEntry;
import com.lpzcahuillan.queue.exception.ResourceNotFoundException;
import com.lpzcahuillan.queue.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final QueueRepository repository;

    @Override
    public QueueResponse addToQueue(QueueRequest request) {
        log.info("Agregando cliente a la cola: nombre={}, tamaño de grupo={}", request.getCustomerName(), request.getPartySize());
        long currentWaiting = repository.countByStatus(QueueEntry.QueueStatus.WAITING);
        QueueEntry entry = QueueEntry.builder()
                .customerName(request.getCustomerName())
                .partySize(request.getPartySize())
                .entryTime(LocalDateTime.now())
                .status(QueueEntry.QueueStatus.WAITING)
                .queuePosition((int) currentWaiting + 1)
                .build();
        QueueEntry saved = repository.save(entry);
        log.info("Cliente agregado a la cola con posición: {}, id: {}", saved.getQueuePosition(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public QueueResponse getNextInQueue() {
        log.debug("Obteniendo siguiente cliente en la cola");
        QueueEntry entry = repository.findFirstByStatusOrderByQueuePositionAsc(QueueEntry.QueueStatus.WAITING)
                .orElseThrow(() -> {
                    log.warn("No hay clientes esperando en la cola");
                    return new ResourceNotFoundException("No one is waiting in queue");
                });
        entry.setStatus(QueueEntry.QueueStatus.CALLED);
        repository.save(entry);
        updateQueuePositions();
        log.info("Cliente llamado de la cola: id={}, nombre={}", entry.getId(), entry.getCustomerName());
        return mapToResponse(entry);
    }

    @Override
    public QueueResponse getById(Long id) {
        log.debug("Obteniendo entrada de cola con id: {}", id);
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.warn("Entrada de cola no encontrada con id: {}", id);
                    return new ResourceNotFoundException("Queue entry not found");
                });
    }

    @Override
    public List<QueueResponse> getWaitingQueue() {
        log.debug("Obteniendo todos los clientes esperando en la cola");
        List<QueueResponse> waitingList = repository.findByStatusOrderByQueuePositionAsc(QueueEntry.QueueStatus.WAITING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Se obtuvieron {} clientes esperando en la cola", waitingList.size());
        return waitingList;
    }

    @Override
    public QueueResponse updateStatus(Long id, String status) {
        log.info("Actualizando estado de entrada de cola {} a: {}", id, status);
        QueueEntry entry = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Entrada de cola no encontrada con id: {} durante la actualización", id);
                    return new ResourceNotFoundException("Queue entry not found");
                });
        entry.setStatus(QueueEntry.QueueStatus.valueOf(status.toUpperCase()));
        repository.save(entry);
        if (!entry.getStatus().equals(QueueEntry.QueueStatus.WAITING)) {
            updateQueuePositions();
        }
        log.info("Estado de entrada de cola {} actualizado exitosamente", id);
        return mapToResponse(entry);
    }

    @Override
    public void removeFromQueue(Long id) {
        log.info("Eliminando entrada de cola con id: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Entrada de cola no encontrada con id: {} durante la eliminación", id);
            throw new ResourceNotFoundException("Queue entry not found");
        }
        repository.deleteById(id);
        updateQueuePositions();
        log.info("Entrada de cola {} eliminada exitosamente y posiciones actualizadas", id);
    }

    private void updateQueuePositions() {
        List<QueueEntry> waiting = repository.findByStatusOrderByQueuePositionAsc(QueueEntry.QueueStatus.WAITING);
        for (int i = 0; i < waiting.size(); i++) {
            waiting.get(i).setQueuePosition(i + 1);
        }
        repository.saveAll(waiting);
    }

    private QueueResponse mapToResponse(QueueEntry entry) {
        return QueueResponse.builder()
                .id(entry.getId())
                .customerName(entry.getCustomerName())
                .partySize(entry.getPartySize())
                .entryTime(entry.getEntryTime())
                .status(entry.getStatus())
                .queuePosition(entry.getQueuePosition())
                .build();
    }
}

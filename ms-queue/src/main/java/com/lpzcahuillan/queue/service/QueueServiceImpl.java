package com.lpzcahuillan.queue.service;

import com.lpzcahuillan.queue.dto.QueueRequest;
import com.lpzcahuillan.queue.dto.QueueResponse;
import com.lpzcahuillan.queue.entity.QueueEntry;
import com.lpzcahuillan.queue.exception.ResourceNotFoundException;
import com.lpzcahuillan.queue.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final QueueRepository repository;

    @Override
    public QueueResponse addToQueue(QueueRequest request) {
        long currentWaiting = repository.countByStatus(QueueEntry.QueueStatus.WAITING);
        QueueEntry entry = QueueEntry.builder()
                .customerName(request.getCustomerName())
                .partySize(request.getPartySize())
                .entryTime(LocalDateTime.now())
                .status(QueueEntry.QueueStatus.WAITING)
                .queuePosition((int) currentWaiting + 1)
                .build();
        return mapToResponse(repository.save(entry));
    }

    @Override
    public QueueResponse getNextInQueue() {
        QueueEntry entry = repository.findFirstByStatusOrderByQueuePositionAsc(QueueEntry.QueueStatus.WAITING)
                .orElseThrow(() -> new ResourceNotFoundException("No one is waiting in queue"));
        entry.setStatus(QueueEntry.QueueStatus.CALLED);
        repository.save(entry);
        updateQueuePositions();
        return mapToResponse(entry);
    }

    @Override
    public QueueResponse getById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));
    }

    @Override
    public List<QueueResponse> getWaitingQueue() {
        return repository.findByStatusOrderByQueuePositionAsc(QueueEntry.QueueStatus.WAITING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QueueResponse updateStatus(Long id, String status) {
        QueueEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queue entry not found"));
        entry.setStatus(QueueEntry.QueueStatus.valueOf(status.toUpperCase()));
        repository.save(entry);
        if (!entry.getStatus().equals(QueueEntry.QueueStatus.WAITING)) {
            updateQueuePositions();
        }
        return mapToResponse(entry);
    }

    @Override
    public void removeFromQueue(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Queue entry not found");
        }
        repository.deleteById(id);
        updateQueuePositions();
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

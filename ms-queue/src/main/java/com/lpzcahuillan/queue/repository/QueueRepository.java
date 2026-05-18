package com.lpzcahuillan.queue.repository;

import com.lpzcahuillan.queue.entity.QueueEntry;
import com.lpzcahuillan.queue.entity.QueueEntry.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QueueRepository extends JpaRepository<QueueEntry, Long> {
    List<QueueEntry> findByStatusOrderByQueuePositionAsc(QueueStatus status);
    Optional<QueueEntry> findFirstByStatusOrderByQueuePositionAsc(QueueStatus status);
    long countByStatus(QueueStatus status);
}

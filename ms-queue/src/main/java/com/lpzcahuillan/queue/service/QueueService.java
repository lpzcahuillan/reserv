package com.lpzcahuillan.queue.service;

import com.lpzcahuillan.queue.dto.QueueRequest;
import com.lpzcahuillan.queue.dto.QueueResponse;
import java.util.List;

public interface QueueService {
    QueueResponse addToQueue(QueueRequest request);
    QueueResponse getNextInQueue();
    QueueResponse getById(Long id);
    List<QueueResponse> getWaitingQueue();
    QueueResponse updateStatus(Long id, String status);
    void removeFromQueue(Long id);
}

package com.lpzcahuillan.queue.controller;

import com.lpzcahuillan.queue.dto.QueueRequest;
import com.lpzcahuillan.queue.dto.QueueResponse;
import com.lpzcahuillan.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService service;

    @PostMapping
    public ResponseEntity<QueueResponse> addToQueue(@RequestBody QueueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addToQueue(request));
    }

    @PostMapping("/next")
    public ResponseEntity<QueueResponse> nextInQueue() {
        return ResponseEntity.ok(service.getNextInQueue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueueResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<QueueResponse>> getWaiting() {
        return ResponseEntity.ok(service.getWaitingQueue());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<QueueResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        service.removeFromQueue(id);
        return ResponseEntity.noContent().build();
    }
}

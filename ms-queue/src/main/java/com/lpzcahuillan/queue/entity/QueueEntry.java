package com.lpzcahuillan.queue.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private Integer partySize;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    @Enumerated(EnumType.STRING)
    private QueueStatus status;

    private Integer queuePosition;

    public enum QueueStatus {
        WAITING, CALLED, SEATED, CANCELLED, NO_SHOW
    }
}

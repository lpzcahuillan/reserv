package com.lpzcahuillan.queue.dto;

import com.lpzcahuillan.queue.entity.QueueEntry.QueueStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueResponse {
    private Long id;
    private String customerName;
    private Integer partySize;
    private LocalDateTime entryTime;
    private QueueStatus status;
    private Integer queuePosition;
}

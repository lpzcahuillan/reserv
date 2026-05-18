package com.lpzcahuillan.queue.dto;

import com.lpzcahuillan.queue.entity.QueueEntry.QueueStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueRequest {
    private String customerName;
    private Integer partySize;
    private QueueStatus status;
}

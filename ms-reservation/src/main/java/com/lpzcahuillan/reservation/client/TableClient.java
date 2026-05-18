package com.lpzcahuillan.reservation.client;

import com.lpzcahuillan.reservation.dto.TableDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-table")
public interface TableClient {
    @GetMapping("/api/tables/{id}")
    TableDTO getTableById(@PathVariable("id") Long id);
}

package com.lpzcahuillan.reservation.client.fallback;

import com.lpzcahuillan.reservation.client.TableClient;
import com.lpzcahuillan.reservation.dto.TableDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TableClientFallback implements TableClient {
    @Override
    public TableDTO getTableById(Long id) {
        log.warn("Servicio ms-table no disponible. Retornando mesa temporal para id: {}", id);
        TableDTO fallbackTable = new TableDTO();
        fallbackTable.setId(id);
        fallbackTable.setTableNumber("0");
        fallbackTable.setCapacity(4);
        fallbackTable.setStatus("FREE");
        return fallbackTable;
    }
}

package com.lpzcahuillan.reservation.client.fallback;

import com.lpzcahuillan.reservation.client.TableClient;
import com.lpzcahuillan.reservation.dto.TableDTO;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TableClientFallbackFactory implements FallbackFactory<TableClient> {

    @Override
    public TableClient create(Throwable cause) {
        return new TableClient() {
            @Override
            public TableDTO getTableById(Long id) {
                if (cause instanceof FeignException && ((FeignException) cause).status() == 404) {
                    log.warn("Mesa con ID {} no existe en ms-table (404)", id);
                    throw (FeignException) cause;
                }
                log.warn("Servicio ms-table no disponible. Fallback disparado por: {}", cause.getMessage());
                TableDTO fallbackTable = new TableDTO();
                fallbackTable.setId(id);
                fallbackTable.setTableNumber("0");
                fallbackTable.setCapacity(4);
                fallbackTable.setStatus("FREE");
                return fallbackTable;
            }
        };
    }
}

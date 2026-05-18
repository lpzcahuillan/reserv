package com.lpzcahuillan.table.service;

import com.lpzcahuillan.table.dto.TableRequest;
import com.lpzcahuillan.table.dto.TableResponse;
import com.lpzcahuillan.table.entity.RestaurantTable;
import com.lpzcahuillan.table.exception.BadRequestException;
import com.lpzcahuillan.table.exception.ResourceNotFoundException;
import com.lpzcahuillan.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {

    private final TableRepository repository;

    @Override
    public TableResponse createTable(TableRequest request) {
        log.info("Creando mesa con número: {}", request.getTableNumber());
        if (repository.existsByTableNumber(request.getTableNumber())) {
            log.warn("Intento de crear mesa con número existente: {}", request.getTableNumber());
            throw new BadRequestException("Table number already exists");
        }
        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : RestaurantTable.TableStatus.AVAILABLE)
                .build();
        RestaurantTable saved = repository.save(table);
        log.info("Mesa creada exitosamente con id: {} y número: {}", saved.getId(), saved.getTableNumber());
        return mapToResponse(saved);
    }

    @Override
    public TableResponse getTableById(Long id) {
        log.debug("Obteniendo mesa con id: {}", id);
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.warn("Mesa no encontrada con id: {}", id);
                    return new ResourceNotFoundException("Table not found");
                });
    }

    @Override
    public List<TableResponse> getAllTables() {
        log.debug("Obteniendo todas las mesas");
        List<TableResponse> tables = repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Se obtuvieron {} mesas", tables.size());
        return tables;
    }

    @Override
    public List<TableResponse> getTablesByCapacity(Integer capacity) {
        log.debug("Obteniendo mesas con capacidad >= {}", capacity);
        List<TableResponse> tables = repository.findByCapacityGreaterThanEqual(capacity).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Se obtuvieron {} mesas con capacidad >= {}", tables.size(), capacity);
        return tables;
    }

    @Override
    public TableResponse updateTable(Long id, TableRequest request) {
        log.info("Actualizando mesa con id: {}", id);
        RestaurantTable table = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Mesa no encontrada con id: {} durante la actualización", id);
                    return new ResourceNotFoundException("Table not found");
                });

        if (!table.getTableNumber().equals(request.getTableNumber()) && repository.existsByTableNumber(request.getTableNumber())) {
            log.warn("Intento de actualizar mesa {} con número existente: {}", id, request.getTableNumber());
            throw new BadRequestException("Table number already exists");
        }

        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());

        repository.save(table);
        log.info("Mesa con id: {} actualizada exitosamente", id);
        return mapToResponse(table);
    }

    @Override
    public void deleteTable(Long id) {
        log.info("Eliminando mesa con id: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Mesa no encontrada con id: {} durante la eliminación", id);
            throw new ResourceNotFoundException("Table not found");
        }
        repository.deleteById(id);
        log.info("Mesa con id: {} eliminada exitosamente", id);
    }

    private TableResponse mapToResponse(RestaurantTable table) {
        return TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .status(table.getStatus())
                .build();
    }
}

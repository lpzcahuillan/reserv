package com.lpzcahuillan.table.service;

import com.lpzcahuillan.table.dto.TableRequest;
import com.lpzcahuillan.table.dto.TableResponse;
import com.lpzcahuillan.table.entity.RestaurantTable;
import com.lpzcahuillan.table.exception.BadRequestException;
import com.lpzcahuillan.table.exception.ResourceNotFoundException;
import com.lpzcahuillan.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {

    private final TableRepository repository;

    @Override
    public TableResponse createTable(TableRequest request) {
        if (repository.existsByTableNumber(request.getTableNumber())) {
            throw new BadRequestException("Table number already exists");
        }
        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : RestaurantTable.TableStatus.AVAILABLE)
                .build();
        return mapToResponse(repository.save(table));
    }

    @Override
    public TableResponse getTableById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
    }

    @Override
    public List<TableResponse> getAllTables() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TableResponse> getTablesByCapacity(Integer capacity) {
        return repository.findByCapacityGreaterThanEqual(capacity).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TableResponse updateTable(Long id, TableRequest request) {
        RestaurantTable table = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        if (!table.getTableNumber().equals(request.getTableNumber()) && repository.existsByTableNumber(request.getTableNumber())) {
            throw new BadRequestException("Table number already exists");
        }

        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());

        return mapToResponse(repository.save(table));
    }

    @Override
    public void deleteTable(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Table not found");
        }
        repository.deleteById(id);
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

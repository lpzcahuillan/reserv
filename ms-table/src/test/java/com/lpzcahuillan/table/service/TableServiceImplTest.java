package com.lpzcahuillan.table.service;

import com.lpzcahuillan.table.dto.TableRequest;
import com.lpzcahuillan.table.dto.TableResponse;
import com.lpzcahuillan.table.entity.RestaurantTable;
import com.lpzcahuillan.table.exception.BadRequestException;
import com.lpzcahuillan.table.exception.ResourceNotFoundException;
import com.lpzcahuillan.table.repository.TableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TableServiceImplTest {

    @Mock
    private TableRepository repository;

    @InjectMocks
    private TableServiceImpl service;

    @Test
    void createTable_Success() {
        // Given
        TableRequest request = TableRequest.builder()
                .tableNumber("MESA-10")
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        RestaurantTable savedTable = RestaurantTable.builder()
                .id(1L)
                .tableNumber("MESA-10")
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        when(repository.existsByTableNumber("MESA-10")).thenReturn(false);
        when(repository.save(any(RestaurantTable.class))).thenReturn(savedTable);

        // When
        TableResponse response = service.createTable(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("MESA-10", response.getTableNumber());
        assertEquals(RestaurantTable.TableStatus.AVAILABLE, response.getStatus());
        verify(repository).save(any(RestaurantTable.class));
    }

    @Test
    void createTable_TableNumberAlreadyExists() {
        // Given
        TableRequest request = TableRequest.builder()
                .tableNumber("MESA-10")
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        when(repository.existsByTableNumber("MESA-10")).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.createTable(request));
        assertEquals("Table number already exists", exception.getMessage());
        verify(repository, never()).save(any(RestaurantTable.class));
    }

    @Test
    void getTableById_Success() {
        // Given
        Long tableId = 1L;
        RestaurantTable table = RestaurantTable.builder()
                .id(tableId)
                .tableNumber("MESA-10")
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        when(repository.findById(tableId)).thenReturn(Optional.of(table));

        // When
        TableResponse response = service.getTableById(tableId);

        // Then
        assertNotNull(response);
        assertEquals(tableId, response.getId());
        assertEquals("MESA-10", response.getTableNumber());
    }

    @Test
    void getTableById_NotFound() {
        // Given
        Long tableId = 99L;
        when(repository.findById(tableId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.getTableById(tableId));
        assertEquals("Table not found", exception.getMessage());
    }
}

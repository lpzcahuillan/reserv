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

    @Test
    void getAllTables_Success() {
        // Given
        RestaurantTable table1 = RestaurantTable.builder().id(1L).tableNumber("MESA-1").capacity(2).status(RestaurantTable.TableStatus.AVAILABLE).build();
        RestaurantTable table2 = RestaurantTable.builder().id(2L).tableNumber("MESA-2").capacity(4).status(RestaurantTable.TableStatus.OCCUPIED).build();
        when(repository.findAll()).thenReturn(java.util.List.of(table1, table2));

        // When
        java.util.List<TableResponse> response = service.getAllTables();

        // Then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("MESA-1", response.get(0).getTableNumber());
        assertEquals(RestaurantTable.TableStatus.OCCUPIED, response.get(1).getStatus());
    }

    @Test
    void getTablesByCapacity_Success() {
        // Given
        RestaurantTable table1 = RestaurantTable.builder().id(1L).tableNumber("MESA-1").capacity(4).status(RestaurantTable.TableStatus.AVAILABLE).build();
        RestaurantTable table2 = RestaurantTable.builder().id(2L).tableNumber("MESA-2").capacity(6).status(RestaurantTable.TableStatus.AVAILABLE).build();
        when(repository.findByCapacityGreaterThanEqual(4)).thenReturn(java.util.List.of(table1, table2));

        // When
        java.util.List<TableResponse> response = service.getTablesByCapacity(4);

        // Then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(4, response.get(0).getCapacity());
        assertEquals(6, response.get(1).getCapacity());
    }

    @Test
    void updateTable_Success() {
        // Given
        Long tableId = 1L;
        TableRequest request = TableRequest.builder()
                .tableNumber("MESA-10-UPDATED")
                .capacity(6)
                .status(RestaurantTable.TableStatus.OCCUPIED)
                .build();

        RestaurantTable existingTable = RestaurantTable.builder()
                .id(tableId)
                .tableNumber("MESA-10")
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        when(repository.findById(tableId)).thenReturn(Optional.of(existingTable));
        when(repository.existsByTableNumber("MESA-10-UPDATED")).thenReturn(false);

        // When
        TableResponse response = service.updateTable(tableId, request);

        // Then
        assertNotNull(response);
        assertEquals("MESA-10-UPDATED", response.getTableNumber());
        assertEquals(6, response.getCapacity());
        assertEquals(RestaurantTable.TableStatus.OCCUPIED, response.getStatus());
        verify(repository).save(existingTable);
    }

    @Test
    void updateTable_TableNumberSame_Success() {
        // Given
        Long tableId = 1L;
        TableRequest request = TableRequest.builder()
                .tableNumber("MESA-10")
                .capacity(6)
                .status(RestaurantTable.TableStatus.OCCUPIED)
                .build();

        RestaurantTable existingTable = RestaurantTable.builder()
                .id(tableId)
                .tableNumber("MESA-10")
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        when(repository.findById(tableId)).thenReturn(Optional.of(existingTable));

        // When
        TableResponse response = service.updateTable(tableId, request);

        // Then
        assertNotNull(response);
        assertEquals("MESA-10", response.getTableNumber());
        assertEquals(6, response.getCapacity());
        assertEquals(RestaurantTable.TableStatus.OCCUPIED, response.getStatus());
        verify(repository).save(existingTable);
        verify(repository, never()).existsByTableNumber(any());
    }

    @Test
    void updateTable_NotFound() {
        // Given
        Long tableId = 99L;
        TableRequest request = TableRequest.builder().tableNumber("MESA-99").build();
        when(repository.findById(tableId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.updateTable(tableId, request));
        assertEquals("Table not found", exception.getMessage());
    }

    @Test
    void updateTable_TableNumberAlreadyExists() {
        // Given
        Long tableId = 1L;
        TableRequest request = TableRequest.builder()
                .tableNumber("MESA-20")
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        RestaurantTable existingTable = RestaurantTable.builder()
                .id(tableId)
                .tableNumber("MESA-10")
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        when(repository.findById(tableId)).thenReturn(Optional.of(existingTable));
        when(repository.existsByTableNumber("MESA-20")).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.updateTable(tableId, request));
        assertEquals("Table number already exists", exception.getMessage());
    }

    @Test
    void deleteTable_Success() {
        // Given
        Long tableId = 1L;
        when(repository.existsById(tableId)).thenReturn(true);
        doNothing().when(repository).deleteById(tableId);

        // When
        assertDoesNotThrow(() -> service.deleteTable(tableId));

        // Then
        verify(repository).deleteById(tableId);
    }

    @Test
    void deleteTable_NotFound() {
        // Given
        Long tableId = 99L;
        when(repository.existsById(tableId)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.deleteTable(tableId));
        assertEquals("Table not found", exception.getMessage());
        verify(repository, never()).deleteById(tableId);
    }
}

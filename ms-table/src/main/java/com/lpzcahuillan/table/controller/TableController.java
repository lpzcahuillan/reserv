package com.lpzcahuillan.table.controller;

import com.lpzcahuillan.table.dto.TableRequest;
import com.lpzcahuillan.table.dto.TableResponse;
import com.lpzcahuillan.table.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService service;

    @PostMapping
    public ResponseEntity<TableResponse> create(@RequestBody TableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTable(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTableById(id));
    }

    @GetMapping
    public ResponseEntity<List<TableResponse>> getAll() {
        return ResponseEntity.ok(service.getAllTables());
    }

    @GetMapping("/capacity/{capacity}")
    public ResponseEntity<List<TableResponse>> getByCapacity(@PathVariable Integer capacity) {
        return ResponseEntity.ok(service.getTablesByCapacity(capacity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableResponse> update(@PathVariable Long id, @RequestBody TableRequest request) {
        return ResponseEntity.ok(service.updateTable(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}

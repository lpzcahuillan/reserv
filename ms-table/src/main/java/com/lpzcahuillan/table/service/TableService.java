package com.lpzcahuillan.table.service;

import com.lpzcahuillan.table.dto.TableRequest;
import com.lpzcahuillan.table.dto.TableResponse;
import java.util.List;

public interface TableService {
    TableResponse createTable(TableRequest request);
    TableResponse getTableById(Long id);
    List<TableResponse> getAllTables();
    List<TableResponse> getTablesByCapacity(Integer capacity);
    TableResponse updateTable(Long id, TableRequest request);
    void deleteTable(Long id);
}

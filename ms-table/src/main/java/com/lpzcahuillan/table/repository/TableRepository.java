package com.lpzcahuillan.table.repository;

import com.lpzcahuillan.table.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByCapacityGreaterThanEqual(Integer capacity);
    boolean existsByTableNumber(String tableNumber);
}

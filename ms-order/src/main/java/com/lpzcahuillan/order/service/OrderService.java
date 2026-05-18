package com.lpzcahuillan.order.service;

import com.lpzcahuillan.order.dto.OrderRequest;
import com.lpzcahuillan.order.dto.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrderStatus(Long id, String status);
    void deleteOrder(Long id);
}

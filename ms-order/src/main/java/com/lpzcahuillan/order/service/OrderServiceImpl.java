package com.lpzcahuillan.order.service;

import com.lpzcahuillan.order.client.MenuClient;
import com.lpzcahuillan.order.dto.MenuItemDTO;
import com.lpzcahuillan.order.dto.OrderRequest;
import com.lpzcahuillan.order.dto.OrderResponse;
import com.lpzcahuillan.order.entity.Order;
import com.lpzcahuillan.order.entity.OrderItem;
import com.lpzcahuillan.order.exception.ResourceNotFoundException;
import com.lpzcahuillan.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final MenuClient menuClient;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        Order order = Order.builder()
                .tableId(request.getTableId())
                .orderTime(LocalDateTime.now())
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            MenuItemDTO menuItem = menuClient.getMenuItemById(itemReq.getMenuItemId());
            
            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            OrderItem item = OrderItem.builder()
                    .menuItemId(menuItem.getId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(subtotal)
                    .order(order)
                    .build();
            order.getItems().add(item);
        }
        order.setTotalAmount(total);

        return mapToResponse(repository.save(order));
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        return mapToResponse(repository.save(order));
    }

    @Override
    public void deleteOrder(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found");
        }
        repository.deleteById(id);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .tableId(order.getTableId())
                .orderTime(order.getOrderTime())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream().map(item -> 
                    OrderResponse.OrderItemResponse.builder()
                            .id(item.getId())
                            .menuItemId(item.getMenuItemId())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(item.getSubtotal())
                            .build()
                ).collect(Collectors.toList()))
                .build();
    }
}

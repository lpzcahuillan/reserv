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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final MenuClient menuClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creando orden para mesa: {}, cantidad de items: {}", request.getTableId(), request.getItems().size());
        Order order = Order.builder()
                .tableId(request.getTableId())
                .orderTime(LocalDateTime.now())
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            log.debug("Procesando item de orden: menuItemId={}, cantidad={}", itemReq.getMenuItemId(), itemReq.getQuantity());
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

        Order saved = repository.save(order);
        log.info("Orden creada exitosamente con id: {}, mesa: {}, total: {}", saved.getId(), saved.getTableId(), saved.getTotalAmount());

        // Publicar evento en RabbitMQ
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "ORDER_CREATED");
            event.put("message", "¡Nueva comanda/orden registrada!");
            event.put("details", String.format("Orden ID: %d, Mesa ID: %d, Total: %s",
                    saved.getId(), saved.getTableId(), saved.getTotalAmount()));
            rabbitTemplate.convertAndSend("notification.queue", event);
            log.info("Evento ORDER_CREATED publicado en RabbitMQ");
        } catch (Exception e) {
            log.error("Fallo al publicar evento ORDER_CREATED en RabbitMQ", e);
        }

        return mapToResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        log.debug("Obteniendo orden con id: {}", id);
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.warn("Orden no encontrada con id: {}", id);
                    return new ResourceNotFoundException("Order not found");
                });
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        log.debug("Obteniendo todas las órdenes");
        List<OrderResponse> orders = repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("Se obtuvieron {} órdenes", orders.size());
        return orders;
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, String status) {
        log.info("Actualizando estado de orden {} a: {}", id, status);
        Order order = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Orden no encontrada con id: {} durante actualizar estado", id);
                    return new ResourceNotFoundException("Order not found");
                });
        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        Order updated = repository.save(order);
        log.info("Estado de orden {} actualizado exitosamente a: {}", id, updated.getStatus());
        return mapToResponse(updated);
    }

    @Override
    public void deleteOrder(Long id) {
        log.info("Eliminando orden con id: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Orden no encontrada con id: {} durante la eliminación", id);
            throw new ResourceNotFoundException("Order not found");
        }
        repository.deleteById(id);
        log.info("Orden con id: {} eliminada exitosamente", id);
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

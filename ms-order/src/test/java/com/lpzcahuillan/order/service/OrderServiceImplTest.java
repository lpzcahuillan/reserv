package com.lpzcahuillan.order.service;

import com.lpzcahuillan.order.client.MenuClient;
import com.lpzcahuillan.order.dto.MenuItemDTO;
import com.lpzcahuillan.order.dto.OrderRequest;
import com.lpzcahuillan.order.dto.OrderResponse;
import com.lpzcahuillan.order.entity.Order;
import com.lpzcahuillan.order.entity.OrderItem;
import com.lpzcahuillan.order.exception.ResourceNotFoundException;
import com.lpzcahuillan.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private MenuClient menuClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderServiceImpl service;

    @Test
    void createOrder_Success() {
        // Given
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest(10L, 2);
        OrderRequest request = OrderRequest.builder()
                .tableId(1L)
                .items(List.of(itemReq))
                .build();

        MenuItemDTO menuItem = new MenuItemDTO();
        menuItem.setId(10L);
        menuItem.setName("Burguer");
        menuItem.setPrice(new BigDecimal("10.00"));

        Order savedOrder = Order.builder()
                .id(100L)
                .tableId(1L)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("20.00"))
                .items(new ArrayList<>())
                .build();
        
        OrderItem orderItem = OrderItem.builder()
                .id(50L)
                .menuItemId(10L)
                .quantity(2)
                .unitPrice(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("20.00"))
                .order(savedOrder)
                .build();
        savedOrder.getItems().add(orderItem);

        when(menuClient.getMenuItemById(10L)).thenReturn(menuItem);
        when(repository.save(any(Order.class))).thenReturn(savedOrder);
        doNothing().when(rabbitTemplate).convertAndSend(eq("notification.queue"), anyMap());

        // When
        OrderResponse response = service.createOrder(request);

        // Then
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getTableId());
        assertEquals(new BigDecimal("20.00"), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
        assertEquals(10L, response.getItems().get(0).getMenuItemId());
        assertEquals(2, response.getItems().get(0).getQuantity());

        verify(menuClient).getMenuItemById(10L);
        verify(repository).save(any(Order.class));
        verify(rabbitTemplate).convertAndSend(eq("notification.queue"), anyMap());
    }

    @Test
    void createOrder_MenuClientThrowsException() {
        // Given
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest(99L, 1);
        OrderRequest request = OrderRequest.builder()
                .tableId(1L)
                .items(List.of(itemReq))
                .build();

        when(menuClient.getMenuItemById(99L)).thenThrow(new RuntimeException("Menu item not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> service.createOrder(request));
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_Success() {
        // Given
        Long id = 100L;
        Order order = Order.builder()
                .id(id)
                .tableId(1L)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("15.00"))
                .items(Collections.emptyList())
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(order));

        // When
        OrderResponse response = service.getOrderById(id);

        // Then
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(Order.OrderStatus.PENDING, response.getStatus());
    }

    @Test
    void getOrderById_NotFound() {
        // Given
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.getOrderById(id));
        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void getAllOrders_Success() {
        // Given
        Order order1 = Order.builder().id(1L).items(Collections.emptyList()).build();
        Order order2 = Order.builder().id(2L).items(Collections.emptyList()).build();
        when(repository.findAll()).thenReturn(List.of(order1, order2));

        // When
        List<OrderResponse> response = service.getAllOrders();

        // Then
        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void updateOrderStatus_Success() {
        // Given
        Long id = 100L;
        Order order = Order.builder()
                .id(id)
                .status(Order.OrderStatus.PENDING)
                .items(Collections.emptyList())
                .build();

        Order updatedOrder = Order.builder()
                .id(id)
                .status(Order.OrderStatus.DELIVERED)
                .items(Collections.emptyList())
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(updatedOrder);

        // When
        OrderResponse response = service.updateOrderStatus(id, "delivered");

        // Then
        assertNotNull(response);
        assertEquals(Order.OrderStatus.DELIVERED, response.getStatus());
        verify(repository).save(order);
    }

    @Test
    void updateOrderStatus_NotFound() {
        // Given
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.updateOrderStatus(id, "delivered"));
        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void updateOrderStatus_InvalidStatus() {
        // Given
        Long id = 100L;
        Order order = Order.builder()
                .id(id)
                .status(Order.OrderStatus.PENDING)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.updateOrderStatus(id, "invalid_status"));
    }

    @Test
    void deleteOrder_Success() {
        // Given
        Long id = 100L;
        when(repository.existsById(id)).thenReturn(true);
        doNothing().when(repository).deleteById(id);

        // When
        assertDoesNotThrow(() -> service.deleteOrder(id));

        // Then
        verify(repository).deleteById(id);
    }

    @Test
    void deleteOrder_NotFound() {
        // Given
        Long id = 99L;
        when(repository.existsById(id)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.deleteOrder(id));
        assertEquals("Order not found", exception.getMessage());
        verify(repository, never()).deleteById(id);
    }
}

package com.example.Order_Service;

import com.example.Order_Service.Domains.model.Order;
import com.example.Order_Service.Domains.model.OrderItems;
import com.example.Order_Service.Domains.model.OrderStatus;
import com.example.Order_Service.Domains.port.OutputPort.SaveOrderEvent;
import com.example.Order_Service.Domains.port.OutputPort.SaveOrderPort;
import com.example.Order_Service.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Optional;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceApplicationTests {
    private SaveOrderPort saveOrderPort;
    private OrderService orderService;

	@Test
	void contextLoads() {
	}
    @BeforeEach
    void setup() {
        saveOrderPort = mock(SaveOrderPort.class);
        SaveOrderEvent saveOrderEvent = mock(SaveOrderEvent.class);
        orderService = new OrderService(saveOrderPort,saveOrderEvent);
    }
    @Test
    void testPlaceOrder_ShouldCalculateTotalPriceAndSaveOrder() {
        // Arrange
        Order order = new Order();
        order.setCustomerId(1L);
        order.setVendorId(2L);

        OrderItems item1 = new OrderItems();
        item1.setProductId(101L);
        item1.setQuantity(2);
        item1.setUnitPrice(50.0);

        OrderItems item2 = new OrderItems();
        item2.setProductId(102L);
        item2.setQuantity(1);
        item2.setUnitPrice(100.0);

        order.setItems(List.of(item1, item2));

        // Act
        orderService.placeOrder(order);

        // Assert
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(saveOrderPort, times(1)).save(captor.capture());
        Order savedOrder = captor.getValue();

        assertEquals(OrderStatus.PLACED, savedOrder.getStatus());
        assertEquals(200.0, savedOrder.getTotalPrice());
        assertNotNull(savedOrder.getCreatedAt());
    }
    @Test
    void testUpdateOrderStatus_ShouldUpdateExistingOrder() {
        Order existing = new Order();
        existing.setStatus(OrderStatus.PLACED);
        when(saveOrderPort.findById(1L)).thenReturn(Optional.of(existing));

        orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        verify(saveOrderPort, times(1)).save(existing);
        assertEquals(OrderStatus.CONFIRMED, existing.getStatus());
    }

    @Test
    void testUpdateOrderStatus_ShouldThrowWhenNotFound() {
        when(saveOrderPort.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.CANCELLED));
    }

    @Test
    void testGetOrdersByCustomer_ShouldDelegateToPort() {
        when(saveOrderPort.findByCustomerId(1L)).thenReturn(List.of(new Order()));
        var result = orderService.getOrdersByCustomer(1L);
        assertEquals(1, result.size());
        verify(saveOrderPort).findByCustomerId(1L);
    }

    @Test
    void testGetOrdersByVendor_ShouldDelegateToPort() {
        when(saveOrderPort.findByVendorId(2L)).thenReturn(List.of(new Order()));
        var result = orderService.getOrdersByVendor(2L);
        assertEquals(1, result.size());
        verify(saveOrderPort).findByVendorId(2L);
    }
}

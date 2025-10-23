package com.example.Order_Service.Domains.port.InputPort;

import com.example.Order_Service.Domains.model.Order;
import com.example.Order_Service.Domains.model.OrderStatus;

import java.util.List;

public interface OrderUseCase {
    Order placeOrder(Order order);
    void updateOrderStatus(Long orderId, OrderStatus status);
    List<Order> getOrdersByCustomer(Long customerId);
    List<Order> getOrdersByVendor(Long vendorId);
}

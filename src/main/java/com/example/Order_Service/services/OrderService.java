package com.example.Order_Service.services;

import com.example.Order_Service.Domains.model.Order;
import com.example.Order_Service.Domains.model.OrderStatus;
import com.example.Order_Service.Domains.port.InputPort.OrderUseCase;
import com.example.Order_Service.Domains.port.OutputPort.SaveOrderPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService implements OrderUseCase {
    private final SaveOrderPort saveOrderPort;

    public OrderService(SaveOrderPort saveOrderPort) {
        this.saveOrderPort = saveOrderPort;
    }

    @Override
    public Order placeOrder(Order order) {
        double total = order.getItems().stream()
                .mapToDouble(item-> item.getUnitPrice() * item.getQuantity())
                .sum();
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(java.time.Instant.now());
        return saveOrderPort.save(order);
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        var existingOrder = saveOrderPort.findById(orderId)
                .orElseThrow(()->new RuntimeException("Order Not Found"));
        existingOrder.setStatus(status);
        saveOrderPort.save(existingOrder);

    }

    @Override
    public List<Order> getOrdersByCustomer(Long customerId) {
        return saveOrderPort.findByCustomerId(customerId);
    }

    @Override
    public List<Order> getOrdersByVendor(Long vendorId) {
        return saveOrderPort.findByVendorId(vendorId);
    }
}

package com.example.Order_Service.services;

import com.example.Order_Service.Domains.model.Order;
import com.example.Order_Service.Domains.model.OrderStatus;
import com.example.Order_Service.Domains.port.InputPort.OrderUseCase;
import com.example.Order_Service.Domains.port.OutputPort.SaveOrderEvent;
import com.example.Order_Service.Domains.port.OutputPort.SaveOrderPort;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService implements OrderUseCase {
    private final SaveOrderPort saveOrderPort;
    private final SaveOrderEvent saveOrderEvent;

    public OrderService(SaveOrderPort saveOrderPort, SaveOrderEvent saveOrderEvent) {
        this.saveOrderPort = saveOrderPort;
        this.saveOrderEvent = saveOrderEvent;
    }

    @Override
    @Transactional
    public Order placeOrder(Order order) {
        double total = order.getItems().stream()
                .mapToDouble(item-> item.getUnitPrice() * item.getQuantity())
                .sum();
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(java.time.Instant.now());
        Order savedOrder = saveOrderPort.save(order);

        // Save the outbox event
        saveOrderEvent.saveEvent(savedOrder, "Order", "OrderPlaced");

        return savedOrder;

    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        var existingOrder = saveOrderPort.findById(orderId)
                .orElseThrow(()->new RuntimeException("Order Not Found"));
        existingOrder.setStatus(status);
        Order updatedOrder = saveOrderPort.save(existingOrder);

        // Save the outbox event
        saveOrderEvent.saveEvent(updatedOrder, "Order", "OrderUpdated");


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

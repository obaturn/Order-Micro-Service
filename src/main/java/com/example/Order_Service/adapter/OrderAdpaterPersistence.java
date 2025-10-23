package com.example.Order_Service.adapter;

import com.example.Order_Service.Domains.infrastructure.JpaOrderRepository;
import com.example.Order_Service.Domains.model.Order;
import com.example.Order_Service.Domains.port.OutputPort.SaveOrderPort;

import java.util.List;
import java.util.Optional;

public class OrderAdpaterPersistence implements SaveOrderPort {
    private final JpaOrderRepository orderRepository;

    public OrderAdpaterPersistence(JpaOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> findByVendorId(Long vendorId) {
        return orderRepository.findByVendorId(vendorId);
    }
}

package com.example.Order_Service.Domains.port.OutputPort;

import com.example.Order_Service.Domains.model.Order;

import java.util.List;
import java.util.Optional;

public interface SaveOrderPort {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByVendorId(Long vendorId);
}

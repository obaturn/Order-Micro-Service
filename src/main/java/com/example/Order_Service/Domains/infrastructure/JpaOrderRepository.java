package com.example.Order_Service.Domains.infrastructure;

import com.example.Order_Service.Domains.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByVendorId(Long vendorId);
}

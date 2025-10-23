package com.example.Order_Service.Controller;

import com.example.Order_Service.Domains.infrastructure.utils.TenantUtils;
import com.example.Order_Service.Domains.model.Order;
import com.example.Order_Service.Domains.model.OrderStatus;
import com.example.Order_Service.Domains.port.InputPort.OrderUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderUseCase orderUseCase;
    private final TenantUtils tenantUtils;

    public OrderController(OrderUseCase orderUseCase , TenantUtils tenantUtils) {
        this.orderUseCase = orderUseCase;
        this.tenantUtils=tenantUtils;
    }
    @PostMapping("/customer/place")
    @PreAuthorize("hasRole('CUSTOMERS')")
    public ResponseEntity<Order> placeOrder(@RequestBody Order order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("User Authorities "+auth.getAuthorities());
        System.out.println("Authenticated User: " + auth.getName());

        String tenantId = tenantUtils.extractTenantId(auth);
        System.out.println("Tenant ID from token: " + tenantId);
        Long vendorId = parseVendorId(tenantId);
        order.setCustomerId(vendorId);
        return ResponseEntity.ok(orderUseCase.placeOrder(order));
    }
    @PutMapping("/vendor/{orderId}/status")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus status) {
        orderUseCase.updateOrderStatus(orderId, status);
        return ResponseEntity.noContent().build();
    }

    // Customer views their orders
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMERS')")
    public ResponseEntity<List<Order>> getCustomerOrders(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderUseCase.getOrdersByCustomer(customerId));
    }

    // Vendor views all their orders
    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<Order>> getVendorOrders(@PathVariable Long vendorId) {
        return ResponseEntity.ok(orderUseCase.getOrdersByVendor(vendorId));
    }
    private Long parseVendorId(String tenantId) {
        if (tenantId == null) throw new IllegalStateException("tenant_id missing in token");
        // tenantId expected like "vendor1" or "1" — handle both
        try {
            if (tenantId.startsWith("vendor")) {
                return Long.valueOf(tenantId.replace("vendor", ""));
            } else {
                return Long.valueOf(tenantId);
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid tenant_id format: " + tenantId);
        }
    }
}

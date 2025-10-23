package com.example.Order_Service;

import com.example.Order_Service.Controller.OrderController;
import com.example.Order_Service.Domains.infrastructure.utils.TenantUtils;
import com.example.Order_Service.Domains.model.Order;
import com.example.Order_Service.Domains.port.InputPort.OrderUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderUseCase orderUseCase;

    @MockitoBean
    private TenantUtils tenantUtils;

    @Test
    void testPlaceOrder_ShouldReturnOk() throws Exception {
        //  Mock authentication context
        var auth = new UsernamePasswordAuthenticationToken(
                "testUser",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMERS"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        //  Mock service layer + tenant utils
        Mockito.when(orderUseCase.placeOrder(any(Order.class))).thenReturn(new Order());
        Mockito.when(tenantUtils.extractTenantId(any())).thenReturn("vendor1");

        //  Sample request body
        String jsonOrder = """
                {
                    "customerId": 1,
                    "vendorId": 2,
                    "items": [
                        {"productId": 101, "quantity": 2, "unitPrice": 50.0}
                    ]
                }
                """;

        //  Perform test request
        mockMvc.perform(post("/order/customer/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andExpect(status().isOk());
    }
    @Test
    void testGetCustomerOrders_ShouldReturnOk() throws Exception {
        Mockito.when(orderUseCase.getOrdersByCustomer(1L))
                .thenReturn(List.of(new Order()));

        mockMvc.perform(get("/order/customer/1"))
                .andExpect(status().isOk());

        Mockito.verify(orderUseCase).getOrdersByCustomer(1L);
    }

    @Test
    void testGetVendorOrders_ShouldReturnOk() throws Exception {
        Mockito.when(orderUseCase.getOrdersByVendor(2L))
                .thenReturn(List.of(new Order()));

        mockMvc.perform(get("/order/vendor/2"))
                .andExpect(status().isOk());

        Mockito.verify(orderUseCase).getOrdersByVendor(2L);
    }
}

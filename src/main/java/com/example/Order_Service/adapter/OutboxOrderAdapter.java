package com.example.Order_Service.adapter;

import com.example.Order_Service.Domains.infrastructure.OutBoxOrderRepository;
import com.example.Order_Service.Domains.model.OutboxOrderEvent;
import com.example.Order_Service.Domains.port.OutputPort.SaveOrderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OutboxOrderAdapter implements SaveOrderEvent {
    private final OutBoxOrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public OutboxOrderAdapter(OutBoxOrderRepository orderRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }


    @Override
    public void saveEvent(Object payloadObject, String aggregateType, String eventType) {
        try {
            OutboxOrderEvent event = new OutboxOrderEvent();
            event.setAggregateType(aggregateType);
            event.setEventType(eventType);
            event.setPayload(objectMapper.writeValueAsString(payloadObject));
            event.setPublished(false);
            orderRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write outbox event", e);
        }

    }
}

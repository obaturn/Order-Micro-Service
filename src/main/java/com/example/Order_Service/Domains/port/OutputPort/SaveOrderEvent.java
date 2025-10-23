package com.example.Order_Service.Domains.port.OutputPort;

public interface SaveOrderEvent {
    void saveEvent(Object payloadObject, String aggregateType, String eventType);
}


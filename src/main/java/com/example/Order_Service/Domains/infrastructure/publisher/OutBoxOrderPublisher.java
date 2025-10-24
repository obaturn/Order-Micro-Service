package com.example.Order_Service.Domains.infrastructure.publisher;

import com.example.Order_Service.Domains.infrastructure.OutBoxOrderRepository;
import com.example.Order_Service.Domains.model.OutboxOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutBoxOrderPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutBoxOrderPublisher.class);
    private final OutBoxOrderRepository outBoxOrderRepository;
    private final PulsarTemplate<String> pulsarTemplate;

    public OutBoxOrderPublisher(OutBoxOrderRepository outBoxOrderRepository, PulsarTemplate<String> pulsarTemplate) {
        this.outBoxOrderRepository = outBoxOrderRepository;
        this.pulsarTemplate = pulsarTemplate;
    }
    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        var events = outBoxOrderRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxOrderEvent event : events) {
            try {
                pulsarTemplate.send(event.getAggregateType(), event.getPayload());
                event.setPublished(true);
                event.setAttempts(event.getAttempts() + 1);
                outBoxOrderRepository.save(event);
                log.info("Published event id=" + event.getId());
            } catch (Exception ex) {
                event.setAttempts(event.getAttempts() + 1);
                event.setLastError(ex.getMessage());
                outBoxOrderRepository.save(event);
                log.error("Failed to publish id=" + event.getId(), ex);
                // if attempts exceed threshold, optionally publish the event payload to a DLQ topic:
                if (event.getAttempts() >= 5) {
                    try {
                        pulsarTemplate.send("order-topic-dlq", event.getPayload());
                        event.setPublished(true); // mark as handled so we don't loop forever
                        outBoxOrderRepository.save(event);
                        log.info("Moved event to DLQ id=" + event.getId());
                    } catch (Exception dlqEx) {
                        log.error("Failed to publish to DLQ for id=" + event.getId(), dlqEx);
                    }
                }
            }
        }
    }
}

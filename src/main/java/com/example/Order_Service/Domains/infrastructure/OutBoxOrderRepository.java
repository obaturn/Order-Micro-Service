package com.example.Order_Service.Domains.infrastructure;

import com.example.Order_Service.Domains.model.OutboxOrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutBoxOrderRepository extends JpaRepository<OutboxOrderEvent,Long> {
    List<OutboxOrderEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();
}

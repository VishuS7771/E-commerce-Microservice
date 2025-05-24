package com.paymentService.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class PaymentEvent {
    @Id
    private UUID eventId;
    private UUID transactionId;
    private String eventMessage;
    private LocalDateTime timestamp;

    public PaymentEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }

    public PaymentEvent(UUID transactionId, String eventMessage) {
        this.eventId = UUID.randomUUID();
        this.transactionId = transactionId;
        this.eventMessage = eventMessage;
        this.timestamp = LocalDateTime.now();
    }
}
package com.paymentService.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Payment {
    @Id
    private UUID transactionId;
    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private LocalDateTime timestamp;

    public enum PaymentStatus {
        SUCCESS, FAILED, PENDING
    }
}
package com.paymentService.service;

import com.paymentService.client.InventoryClient;
import com.paymentService.domain.Payment;
import com.paymentService.domain.PaymentEvent;
import com.paymentService.repository.PaymentRepository;
import com.paymentService.repository.PaymentEventRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final InventoryClient inventoryClient;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventRepository paymentEventRepository,
                          InventoryClient inventoryClient) {
        this.paymentRepository = paymentRepository;
        this.paymentEventRepository = paymentEventRepository;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    @CircuitBreaker(name = "paymentCircuitBreaker", fallbackMethod = "paymentFallback")
    public Payment processPayment(Payment payment) {
        InventoryClient.InventoryValidationRequest request = new InventoryClient.InventoryValidationRequest();
        request.setProductId(payment.getOrderId());
        request.setQuantity(1);

        boolean stockAvailable = inventoryClient.validateAndLockStock(request);
        if (!stockAvailable) {
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return payment;
        }

        payment.setTransactionId(UUID.randomUUID());
        payment.setTimestamp(LocalDateTime.now());
        payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        PaymentEvent event = new PaymentEvent(payment.getTransactionId(), "Payment processed: " + payment.getTransactionId());
        paymentEventRepository.save(event);

        return payment;
    }

    public Payment getPayment(UUID transactionId) {
        return paymentRepository.findById(transactionId).orElse(null);
    }

    public Payment paymentFallback(Payment payment, Throwable t) {
        payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        paymentRepository.save(payment);
        return payment;
    }
}
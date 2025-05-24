package com.paymentService.service;

import com.paymentService.client.InventoryClient;
import com.paymentService.domain.Payment;
import com.paymentService.domain.PaymentEvent;
import com.paymentService.repository.PaymentEventRepository;
import com.paymentService.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventRepository paymentEventRepository;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        payment = new Payment();
        payment.setOrderId(orderId);
        payment.setCustomerId(UUID.randomUUID());
        payment.setTotalAmount(BigDecimal.valueOf(100));
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        InventoryClient.InventoryValidationRequest request = new InventoryClient.InventoryValidationRequest();
        request.setProductId(orderId);
        request.setQuantity(1);
        when(inventoryClient.validateAndLockStock(any())).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentEventRepository.save(any(PaymentEvent.class))).thenReturn(new PaymentEvent());

        // Act
        Payment result = paymentService.processPayment(payment);

        // Assert
        assertNotNull(result.getTransactionId());
        assertEquals(Payment.PaymentStatus.SUCCESS, result.getPaymentStatus());
        assertNotNull(result.getTimestamp());
        verify(inventoryClient, times(1)).validateAndLockStock(any());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentEventRepository, times(1)).save(any(PaymentEvent.class));
    }

    @Test
    void testProcessPayment_InventoryFailure() {
        // Arrange
        InventoryClient.InventoryValidationRequest request = new InventoryClient.InventoryValidationRequest();
        request.setProductId(orderId);
        request.setQuantity(1);
        when(inventoryClient.validateAndLockStock(any())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        Payment result = paymentService.processPayment(payment);

        // Assert
        assertEquals(Payment.PaymentStatus.FAILED, result.getPaymentStatus());
        assertNull(result.getTransactionId());
        assertNull(result.getTimestamp());
        verify(inventoryClient, times(1)).validateAndLockStock(any());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentEventRepository, never()).save(any(PaymentEvent.class));
    }

    @Test
    void testProcessPayment_CircuitBreakerFallback() {
        // Arrange
        when(inventoryClient.validateAndLockStock(any())).thenThrow(new RuntimeException("Inventory service down"));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        Payment result = paymentService.processPayment(payment);

        // Assert
        assertEquals(Payment.PaymentStatus.FAILED, result.getPaymentStatus());
        assertNull(result.getTransactionId());
        assertNull(result.getTimestamp());
        verify(inventoryClient, times(1)).validateAndLockStock(any());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentEventRepository, never()).save(any(PaymentEvent.class));
    }

    @Test
    void testGetPayment_Found() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        when(paymentRepository.findById(transactionId)).thenReturn(Optional.of(payment));

        // Act
        Payment result = paymentService.getPayment(transactionId);

        // Assert
        assertNotNull(result);
        assertEquals(payment.getOrderId(), result.getOrderId());
        assertEquals(payment.getTotalAmount(), result.getTotalAmount());
        verify(paymentRepository, times(1)).findById(transactionId);
    }

    @Test
    void testGetPayment_NotFound() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        when(paymentRepository.findById(transactionId)).thenReturn(Optional.empty());
        Payment result = paymentService.getPayment(transactionId);
        assertNull(result);
        verify(paymentRepository, times(1)).findById(transactionId);
    }
}
package com.inventory.service;

import com.inventory.domain.Inventory;
import com.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        inventory = new Inventory();
        inventory.setId(UUID.randomUUID());
        inventory.setProductId(productId);
        inventory.setQuantity(10);
        inventory.setLastUpdated(LocalDateTime.now());
    }

    @Test
    void testAddOrUpdateStock_Success() {
        // Arrange
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // Act
        Inventory result = inventoryService.addOrUpdateStock(inventory);

        // Assert
        assertNotNull(result);
        assertEquals(inventory.getProductId(), result.getProductId());
        assertEquals(inventory.getQuantity(), result.getQuantity());
        assertNotNull(result.getLastUpdated());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testGetStock_Found() {
        // Arrange
        when(inventoryRepository.findByProductId(productId)).thenReturn(inventory);

        // Act
        Inventory result = inventoryService.getStock(productId);

        // Assert
        assertNotNull(result);
        assertEquals(inventory.getProductId(), result.getProductId());
        assertEquals(inventory.getQuantity(), result.getQuantity());
        verify(inventoryRepository, times(1)).findByProductId(productId);
    }

    @Test
    void testGetStock_NotFound() {
        // Arrange
        when(inventoryRepository.findByProductId(productId)).thenReturn(null);

        // Act
        Inventory result = inventoryService.getStock(productId);

        // Assert
        assertNull(result);
        verify(inventoryRepository, times(1)).findByProductId(productId);
    }

    @Test
    void testValidateAndLockStock_Success() {
        // Arrange
        when(inventoryRepository.findByProductId(productId)).thenReturn(inventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // Act
        boolean result = inventoryService.validateAndLockStock(productId, 5);

        // Assert
        assertTrue(result);
        assertEquals(5, inventory.getQuantity());
        verify(inventoryRepository, times(1)).findByProductId(productId);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testValidateAndLockStock_InsufficientStock() {
        // Arrange
        when(inventoryRepository.findByProductId(productId)).thenReturn(inventory);

        // Act
        boolean result = inventoryService.validateAndLockStock(productId, 15);

        // Assert
        assertFalse(result);
        verify(inventoryRepository, times(1)).findByProductId(productId);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testValidateAndLockStock_ProductNotFound() {
        // Arrange
        when(inventoryRepository.findByProductId(productId)).thenReturn(null);

        // Act
        boolean result = inventoryService.validateAndLockStock(productId, 5);

        // Assert
        assertFalse(result);
        verify(inventoryRepository, times(1)).findByProductId(productId);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testBulkUpdateStock() {
        // Arrange
        Inventory inventory2 = new Inventory();
        inventory2.setId(UUID.randomUUID());
        inventory2.setProductId(UUID.randomUUID());
        inventory2.setQuantity(20);
        List<Inventory> inventories = Arrays.asList(inventory, inventory2);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // Act
        inventoryService.bulkUpdateStock(inventories);

        // Assert
        verify(inventoryRepository, times(2)).save(any(Inventory.class));
    }
}
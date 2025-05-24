package com.inventory.service;

import com.inventory.domain.Inventory;
import com.inventory.repository.InventoryRepository;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    @Retry(name = "inventoryRetry")
    public Inventory addOrUpdateStock(Inventory inventory) {
        inventory.setLastUpdated(java.time.LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    public Inventory getStock(UUID productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Transactional

    public boolean validateAndLockStock(UUID productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory != null && inventory.getQuantity() >= quantity) {
            inventory.setQuantity(inventory.getQuantity() - quantity);
            inventoryRepository.save(inventory);
            return true;
        }
        return false;
    }

    @Transactional
    public void bulkUpdateStock(List<Inventory> inventories) {
        inventories.forEach(inventory -> inventory.setLastUpdated(java.time.LocalDateTime.now()));
        inventoryRepository.saveAll(inventories);
    }
}
package com.inventory.controller;

import com.inventory.domain.Inventory;
import com.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<Inventory> addOrUpdateStock(@RequestBody Inventory inventory) {
        return ResponseEntity.ok(inventoryService.addOrUpdateStock(inventory));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getStock(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getStock(productId));
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateAndLockStock(@RequestBody InventoryValidationRequest request) {
        return ResponseEntity.ok(inventoryService.validateAndLockStock(request.getProductId(), request.getQuantity()));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> bulkUpdateStock(@RequestBody List<Inventory> inventories) {
        inventoryService.bulkUpdateStock(inventories);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("test api gateway");
    }
}

class InventoryValidationRequest {
    private UUID productId;
    private Integer quantity;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

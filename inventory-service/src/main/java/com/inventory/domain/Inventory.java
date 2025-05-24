package com.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Inventory {
    @Id
    private UUID id;
    private UUID productId;
    private Integer quantity;
    private LocalDateTime lastUpdated;
}

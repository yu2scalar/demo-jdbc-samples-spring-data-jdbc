package com.example.demo_jdbc_samples.repository.springdata;

import com.example.demo_jdbc_samples.model.InventoryItems;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** inventory.items as a Spring Data JDBC aggregate (columns: snake_case of the fields). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "inventory", value = "items")
public class InventoryItemsEntity {
    @Id
    private Integer itemId;
    private String name;
    private Long price;
    private Integer stock;

    public static InventoryItemsEntity of(InventoryItems m) {
        return new InventoryItemsEntity(m.getItemId(), m.getName(), m.getPrice(), m.getStock());
    }

    public InventoryItems toModel() {
        return new InventoryItems(itemId, name, price, stock);
    }
}

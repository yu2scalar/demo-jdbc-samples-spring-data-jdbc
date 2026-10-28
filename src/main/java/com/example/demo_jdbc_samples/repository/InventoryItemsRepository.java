package com.example.demo_jdbc_samples.repository;

import com.example.demo_jdbc_samples.exception.CustomException;
import com.example.demo_jdbc_samples.model.InventoryItems;
import com.example.demo_jdbc_samples.repository.springdata.InventoryItemsEntity;
import com.example.demo_jdbc_samples.repository.springdata.InventoryItemsJdbcRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Limit;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Repository;

/**
 * inventory.items with Spring Data JDBC (InventoryItemsJdbcRepository). Runs in the caller's
 * transaction (TransactionRunner). Same operations and error codes as the ScalarDB version:
 * get of a missing row 9700, update / delete of a missing row 9100.
 */
@Repository
public class InventoryItemsRepository {
    private final InventoryItemsJdbcRepository repository;
    private final JdbcAggregateOperations template;

    private int scanLimit = 100; // Default scan limit

    public InventoryItemsRepository(InventoryItemsJdbcRepository repository, JdbcAggregateOperations template) {
        this.repository = repository;
        this.template = template;
    }

    public void setScanLimit(int scanLimit) {
        this.scanLimit = scanLimit;
    }

    public int getScanLimit() {
        return scanLimit;
    }

    // Get Record by Primary Key
    public InventoryItems getInventoryItems(InventoryItems items) {
        return repository.findById(items.getItemId()).map(InventoryItemsEntity::toModel)
            .orElseThrow(InventoryItemsRepository::notFound);
    }

    // Get Record by Primary Key and lock it until the end of the transaction
    public InventoryItems getInventoryItemsForUpdate(InventoryItems items) {
        return repository.findWithLockByItemId(items.getItemId()).map(InventoryItemsEntity::toModel)
            .orElseThrow(InventoryItemsRepository::notFound);
    }

    private static CustomException notFound() {
        return new CustomException("No record found in InventoryItems", 9700);
    }

    // Insert Record (a duplicate key fails)
    public InventoryItems insertInventoryItems(InventoryItems items) {
        template.insert(InventoryItemsEntity.of(items));
        return items;
    }

    // Update Record (if exists)
    public InventoryItems updateInventoryItems(InventoryItems items) {
        if (repository.updateRow(InventoryItemsEntity.of(items)) == 0) {
            throw new CustomException("Update condition not met: no record in InventoryItems", 9100);
        }
        return items;
    }

    // Upsert Record
    public InventoryItems upsertInventoryItems(InventoryItems items) {
        upsert(InventoryItemsEntity.of(items));
        return items;
    }

    // Delete Record (if exists)
    public void deleteInventoryItems(InventoryItems items) {
        if (repository.deleteRow(items.getItemId()) == 0) {
            throw new CustomException("Delete condition not met: no record in InventoryItems", 9100);
        }
    }

    // Scan All Records (up to scanLimit, no order)
    public List<InventoryItems> getInventoryItemsListAll() {
        return toModels(repository.findBy(Limit.of(scanLimit)));
    }

    // Scan Records by Primary Key
    public List<InventoryItems> getInventoryItemsListByPk(InventoryItems items) {
        return toModels(repository.findByItemId(items.getItemId(), Limit.of(scanLimit)));
    }

    // Update, or insert if there is no such row. A concurrent insert of the same key fails
    // with a duplicate key (Spring Data JDBC has no upsert).
    private void upsert(InventoryItemsEntity entity) {
        if (repository.updateRow(entity) == 0) {
            template.insert(entity);
        }
    }

    public void truncate() {
        repository.truncate();
    }

    private static List<InventoryItems> toModels(List<InventoryItemsEntity> entities) {
        // A mutable list: the callers may sort it
        return entities.stream().map(InventoryItemsEntity::toModel).collect(Collectors.toCollection(ArrayList::new));
    }
}

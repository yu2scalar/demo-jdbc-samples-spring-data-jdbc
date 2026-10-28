package com.example.demo_jdbc_samples.repository;

import com.example.demo_jdbc_samples.exception.CustomException;
import com.example.demo_jdbc_samples.model.OrderingOrders;
import com.example.demo_jdbc_samples.repository.springdata.OrderingOrdersEntity;
import com.example.demo_jdbc_samples.repository.springdata.OrderingOrdersJdbcRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Limit;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Repository;

/**
 * ordering.orders with Spring Data JDBC (OrderingOrdersJdbcRepository). Runs in the caller's
 * transaction (TransactionRunner). Same operations and error codes as the ScalarDB version:
 * get of a missing row 9700, update / delete of a missing row 9100.
 */
@Repository
public class OrderingOrdersRepository {
    private final OrderingOrdersJdbcRepository repository;
    private final JdbcAggregateOperations template;

    private int scanLimit = 100; // Default scan limit

    public OrderingOrdersRepository(OrderingOrdersJdbcRepository repository, JdbcAggregateOperations template) {
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
    public OrderingOrders getOrderingOrders(OrderingOrders orders) {
        return repository.findById(orders.getOrderId()).map(OrderingOrdersEntity::toModel)
            .orElseThrow(OrderingOrdersRepository::notFound);
    }

    // Get Record by Primary Key and lock it until the end of the transaction
    public OrderingOrders getOrderingOrdersForUpdate(OrderingOrders orders) {
        return repository.findWithLockByOrderId(orders.getOrderId()).map(OrderingOrdersEntity::toModel)
            .orElseThrow(OrderingOrdersRepository::notFound);
    }

    private static CustomException notFound() {
        return new CustomException("No record found in OrderingOrders", 9700);
    }

    // Insert Record (a duplicate key fails)
    public OrderingOrders insertOrderingOrders(OrderingOrders orders) {
        template.insert(OrderingOrdersEntity.of(orders));
        return orders;
    }

    // Update Record (if exists)
    public OrderingOrders updateOrderingOrders(OrderingOrders orders) {
        if (repository.updateRow(OrderingOrdersEntity.of(orders)) == 0) {
            throw new CustomException("Update condition not met: no record in OrderingOrders", 9100);
        }
        return orders;
    }

    // Upsert Record
    public OrderingOrders upsertOrderingOrders(OrderingOrders orders) {
        upsert(OrderingOrdersEntity.of(orders));
        return orders;
    }

    // Delete Record (if exists)
    public void deleteOrderingOrders(OrderingOrders orders) {
        if (repository.deleteRow(orders.getOrderId()) == 0) {
            throw new CustomException("Delete condition not met: no record in OrderingOrders", 9100);
        }
    }

    // Scan All Records (up to scanLimit, no order)
    public List<OrderingOrders> getOrderingOrdersListAll() {
        return toModels(repository.findBy(Limit.of(scanLimit)));
    }

    // Scan Records by Primary Key
    public List<OrderingOrders> getOrderingOrdersListByPk(OrderingOrders orders) {
        return toModels(repository.findByOrderId(orders.getOrderId(), Limit.of(scanLimit)));
    }

    // All orders of a customer (index on customer_id; no limit, as the ScalarDB version)
    public List<OrderingOrders> getOrderingOrdersListByCustomerId(int customerId) {
        return toModels(repository.findByCustomerId(customerId));
    }

    // Update, or insert if there is no such row. A concurrent insert of the same key fails
    // with a duplicate key (Spring Data JDBC has no upsert).
    private void upsert(OrderingOrdersEntity entity) {
        if (repository.updateRow(entity) == 0) {
            template.insert(entity);
        }
    }

    public void truncate() {
        repository.truncate();
    }

    private static List<OrderingOrders> toModels(List<OrderingOrdersEntity> entities) {
        // A mutable list: the callers may sort it
        return entities.stream().map(OrderingOrdersEntity::toModel).collect(Collectors.toCollection(ArrayList::new));
    }
}

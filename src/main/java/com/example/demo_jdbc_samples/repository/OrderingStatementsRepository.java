package com.example.demo_jdbc_samples.repository;

import com.example.demo_jdbc_samples.exception.CustomException;
import com.example.demo_jdbc_samples.model.OrderingStatements;
import com.example.demo_jdbc_samples.repository.springdata.OrderingStatementsEntity;
import com.example.demo_jdbc_samples.repository.springdata.OrderingStatementsJdbcRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Limit;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Repository;

/**
 * ordering.statements with Spring Data JDBC (OrderingStatementsJdbcRepository; primary key
 * order_id, line_no as a composite id). Runs in the caller's transaction (TransactionRunner).
 * Same operations and error codes as the ScalarDB version: get of a missing row 9700,
 * update / delete of a missing row 9100.
 */
@Repository
public class OrderingStatementsRepository {
    private final OrderingStatementsJdbcRepository repository;
    private final JdbcAggregateOperations template;

    private int scanLimit = 100; // Default scan limit

    public OrderingStatementsRepository(OrderingStatementsJdbcRepository repository, JdbcAggregateOperations template) {
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
    public OrderingStatements getOrderingStatements(OrderingStatements statements) {
        return repository.findById(new OrderingStatementsEntity.Key(statements.getOrderId(), statements.getLineNo()))
            .map(OrderingStatementsEntity::toModel)
            .orElseThrow(() -> new CustomException("No record found in OrderingStatements", 9700));
    }

    // Insert Record (a duplicate key fails)
    public OrderingStatements insertOrderingStatements(OrderingStatements statements) {
        template.insert(OrderingStatementsEntity.of(statements));
        return statements;
    }

    // Update Record (if exists)
    public OrderingStatements updateOrderingStatements(OrderingStatements statements) {
        if (repository.updateRow(OrderingStatementsEntity.of(statements)) == 0) {
            throw new CustomException("Update condition not met: no record in OrderingStatements", 9100);
        }
        return statements;
    }

    // Upsert Record
    public OrderingStatements upsertOrderingStatements(OrderingStatements statements) {
        upsert(OrderingStatementsEntity.of(statements));
        return statements;
    }

    // Delete Record (if exists)
    public void deleteOrderingStatements(OrderingStatements statements) {
        if (repository.deleteRow(statements.getOrderId(), statements.getLineNo()) == 0) {
            throw new CustomException("Delete condition not met: no record in OrderingStatements", 9100);
        }
    }

    // Scan All Records (up to scanLimit, no order)
    public List<OrderingStatements> getOrderingStatementsListAll() {
        return toModels(repository.findBy(Limit.of(scanLimit)));
    }

    // Scan Records by Partition Key (order_id), in line_no order as the ScalarDB clustering key
    public List<OrderingStatements> getOrderingStatementsListByPk(OrderingStatements statements) {
        return toModels(repository.findByIdOrderIdOrderByIdLineNo(statements.getOrderId(), Limit.of(scanLimit)));
    }

    // Update, or insert if there is no such row. A concurrent insert of the same key fails
    // with a duplicate key (Spring Data JDBC has no upsert).
    private void upsert(OrderingStatementsEntity entity) {
        if (repository.updateRow(entity) == 0) {
            template.insert(entity);
        }
    }

    public void truncate() {
        repository.truncate();
    }

    private static List<OrderingStatements> toModels(List<OrderingStatementsEntity> entities) {
        // A mutable list: the callers may sort it
        return entities.stream().map(OrderingStatementsEntity::toModel).collect(Collectors.toCollection(ArrayList::new));
    }
}

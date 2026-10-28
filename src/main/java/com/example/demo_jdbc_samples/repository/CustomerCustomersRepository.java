package com.example.demo_jdbc_samples.repository;

import com.example.demo_jdbc_samples.exception.CustomException;
import com.example.demo_jdbc_samples.model.CustomerCustomers;
import com.example.demo_jdbc_samples.repository.springdata.CustomerCustomersEntity;
import com.example.demo_jdbc_samples.repository.springdata.CustomerCustomersJdbcRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Limit;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Repository;

/**
 * customer.customers with Spring Data JDBC (CustomerCustomersJdbcRepository). Runs in the
 * caller's transaction (TransactionRunner). Same operations and error codes as the ScalarDB
 * version: get of a missing row 9700, update / delete of a missing row 9100.
 */
@Repository
public class CustomerCustomersRepository {
    private final CustomerCustomersJdbcRepository repository;
    private final JdbcAggregateOperations template;

    private int scanLimit = 100; // Default scan limit

    public CustomerCustomersRepository(CustomerCustomersJdbcRepository repository, JdbcAggregateOperations template) {
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
    public CustomerCustomers getCustomerCustomers(CustomerCustomers customers) {
        return repository.findById(customers.getCustomerId()).map(CustomerCustomersEntity::toModel)
            .orElseThrow(CustomerCustomersRepository::notFound);
    }

    // Get Record by Primary Key and lock it until the end of the transaction
    public CustomerCustomers getCustomerCustomersForUpdate(CustomerCustomers customers) {
        return repository.findWithLockByCustomerId(customers.getCustomerId()).map(CustomerCustomersEntity::toModel)
            .orElseThrow(CustomerCustomersRepository::notFound);
    }

    private static CustomException notFound() {
        return new CustomException("No record found in CustomerCustomers", 9700);
    }

    // Insert Record (a duplicate key fails)
    public CustomerCustomers insertCustomerCustomers(CustomerCustomers customers) {
        template.insert(CustomerCustomersEntity.of(customers));
        return customers;
    }

    // Update Record (if exists)
    public CustomerCustomers updateCustomerCustomers(CustomerCustomers customers) {
        if (repository.updateRow(CustomerCustomersEntity.of(customers)) == 0) {
            throw new CustomException("Update condition not met: no record in CustomerCustomers", 9100);
        }
        return customers;
    }

    // Upsert Record
    public CustomerCustomers upsertCustomerCustomers(CustomerCustomers customers) {
        upsert(CustomerCustomersEntity.of(customers));
        return customers;
    }

    // Delete Record (if exists)
    public void deleteCustomerCustomers(CustomerCustomers customers) {
        if (repository.deleteRow(customers.getCustomerId()) == 0) {
            throw new CustomException("Delete condition not met: no record in CustomerCustomers", 9100);
        }
    }

    // Scan All Records (up to scanLimit, no order)
    public List<CustomerCustomers> getCustomerCustomersListAll() {
        return toModels(repository.findBy(Limit.of(scanLimit)));
    }

    // Scan Records by Primary Key
    public List<CustomerCustomers> getCustomerCustomersListByPk(CustomerCustomers customers) {
        return toModels(repository.findByCustomerId(customers.getCustomerId(), Limit.of(scanLimit)));
    }

    /**
     * Search by name: exact (name = value) or prefix (name LIKE 'value%').
     * limit 0 = no limit. Not locked.
     */
    public List<CustomerCustomers> searchByName(String value, boolean prefix, int limit) {
        Limit l = limit > 0 ? Limit.of(limit) : Limit.unlimited();
        return toModels(prefix ? repository.findByNameStartingWith(value, l) : repository.findByName(value, l));
    }

    // customer_id, name and credit_total of every row (load test check)
    public List<CustomerCustomers> getAllRows() {
        return toModels(repository.findAll());
    }

    // Update, or insert if there is no such row. A concurrent insert of the same key fails
    // with a duplicate key (Spring Data JDBC has no upsert).
    private void upsert(CustomerCustomersEntity entity) {
        if (repository.updateRow(entity) == 0) {
            template.insert(entity);
        }
    }

    public void truncate() {
        repository.truncate();
    }

    private static List<CustomerCustomers> toModels(List<CustomerCustomersEntity> entities) {
        // A mutable list: the callers may sort it
        return entities.stream().map(CustomerCustomersEntity::toModel).collect(Collectors.toCollection(ArrayList::new));
    }
}

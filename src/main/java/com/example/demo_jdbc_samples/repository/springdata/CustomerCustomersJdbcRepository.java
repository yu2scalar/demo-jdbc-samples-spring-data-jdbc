package com.example.demo_jdbc_samples.repository.springdata;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

public interface CustomerCustomersJdbcRepository extends ListCrudRepository<CustomerCustomersEntity, Integer> {

    @Lock(LockMode.PESSIMISTIC_WRITE)
    Optional<CustomerCustomersEntity> findWithLockByCustomerId(Integer customerId);

    List<CustomerCustomersEntity> findBy(Limit limit);

    List<CustomerCustomersEntity> findByCustomerId(Integer customerId, Limit limit);

    List<CustomerCustomersEntity> findByName(String name, Limit limit);

    List<CustomerCustomersEntity> findByNameStartingWith(String prefix, Limit limit);

    @Modifying
    @Query("UPDATE customer.customers SET name = :name, credit_limit = :creditLimit, credit_total = :creditTotal"
        + " WHERE customer_id = :customerId")
    int updateRow(@Param("customerId") Integer customerId, @Param("name") String name,
        @Param("creditLimit") Long creditLimit, @Param("creditTotal") Long creditTotal);

    default int updateRow(CustomerCustomersEntity e) {
        return updateRow(e.getCustomerId(), e.getName(), e.getCreditLimit(), e.getCreditTotal());
    }

    @Modifying
    @Query("DELETE FROM customer.customers WHERE customer_id = :customerId")
    int deleteRow(@Param("customerId") Integer customerId);

    @Modifying
    @Query("TRUNCATE TABLE customer.customers")
    void truncate();
}

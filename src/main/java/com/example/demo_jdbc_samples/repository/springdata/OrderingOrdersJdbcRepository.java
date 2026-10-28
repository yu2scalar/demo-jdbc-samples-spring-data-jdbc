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

public interface OrderingOrdersJdbcRepository extends ListCrudRepository<OrderingOrdersEntity, String> {

    @Lock(LockMode.PESSIMISTIC_WRITE)
    Optional<OrderingOrdersEntity> findWithLockByOrderId(String orderId);

    List<OrderingOrdersEntity> findBy(Limit limit);

    List<OrderingOrdersEntity> findByOrderId(String orderId, Limit limit);

    List<OrderingOrdersEntity> findByCustomerId(Integer customerId);

    @Modifying
    @Query("UPDATE ordering.orders SET customer_id = :customerId, created_at = :createdAt, status = :status,"
        + " total_amount = :totalAmount WHERE order_id = :orderId")
    int updateRow(@Param("orderId") String orderId, @Param("customerId") Integer customerId,
        @Param("createdAt") Long createdAt, @Param("status") String status, @Param("totalAmount") Long totalAmount);

    default int updateRow(OrderingOrdersEntity e) {
        return updateRow(e.getOrderId(), e.getCustomerId(), e.getCreatedAt(), e.getStatus(), e.getTotalAmount());
    }

    @Modifying
    @Query("DELETE FROM ordering.orders WHERE order_id = :orderId")
    int deleteRow(@Param("orderId") String orderId);

    // DELETE, not TRUNCATE: ScalarDB Native JDBC refuses TRUNCATE on a ScalarDB table
    @Modifying
    @Query("DELETE FROM ordering.orders")
    void truncate();
}

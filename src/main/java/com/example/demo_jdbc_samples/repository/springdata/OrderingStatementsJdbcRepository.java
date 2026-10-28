package com.example.demo_jdbc_samples.repository.springdata;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

public interface OrderingStatementsJdbcRepository
        extends ListCrudRepository<OrderingStatementsEntity, OrderingStatementsEntity.Key> {

    List<OrderingStatementsEntity> findBy(Limit limit);

    // In line_no order, as the ScalarDB clustering key
    List<OrderingStatementsEntity> findByIdOrderIdOrderByIdLineNo(String orderId, Limit limit);

    @Modifying
    @Query("UPDATE ordering.statements SET item_id = :itemId, qty = :qty, unit_price = :unitPrice"
        + " WHERE order_id = :orderId AND line_no = :lineNo")
    int updateRow(@Param("orderId") String orderId, @Param("lineNo") Integer lineNo,
        @Param("itemId") Integer itemId, @Param("qty") Integer qty, @Param("unitPrice") Long unitPrice);

    default int updateRow(OrderingStatementsEntity e) {
        return updateRow(e.getId().orderId(), e.getId().lineNo(), e.getItemId(), e.getQty(), e.getUnitPrice());
    }

    @Modifying
    @Query("DELETE FROM ordering.statements WHERE order_id = :orderId AND line_no = :lineNo")
    int deleteRow(@Param("orderId") String orderId, @Param("lineNo") Integer lineNo);

    // DELETE, not TRUNCATE: ScalarDB Native JDBC refuses TRUNCATE on a ScalarDB table
    @Modifying
    @Query("DELETE FROM ordering.statements")
    void truncate();
}

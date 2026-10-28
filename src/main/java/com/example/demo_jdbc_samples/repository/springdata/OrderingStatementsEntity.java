package com.example.demo_jdbc_samples.repository.springdata;

import com.example.demo_jdbc_samples.model.OrderingStatements;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * ordering.statements as a Spring Data JDBC aggregate. The primary key (order_id, line_no)
 * is a composite id (Key), whose fields are columns of the same table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "ordering", value = "statements")
public class OrderingStatementsEntity {
    @Id
    private Key id;
    private Integer itemId;
    private Integer qty;
    private Long unitPrice;

    public record Key(String orderId, Integer lineNo) {
    }

    public static OrderingStatementsEntity of(OrderingStatements m) {
        return new OrderingStatementsEntity(new Key(m.getOrderId(), m.getLineNo()), m.getItemId(), m.getQty(), m.getUnitPrice());
    }

    public OrderingStatements toModel() {
        return new OrderingStatements(id.orderId(), id.lineNo(), itemId, qty, unitPrice);
    }
}

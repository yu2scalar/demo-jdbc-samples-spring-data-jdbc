package com.example.demo_jdbc_samples.repository.springdata;

import com.example.demo_jdbc_samples.model.OrderingOrders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** ordering.orders as a Spring Data JDBC aggregate (columns: snake_case of the fields). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "ordering", value = "orders")
public class OrderingOrdersEntity {
    @Id
    private String orderId;
    private Integer customerId;
    private Long createdAt;
    private String status;
    private Long totalAmount;

    public static OrderingOrdersEntity of(OrderingOrders m) {
        return new OrderingOrdersEntity(m.getOrderId(), m.getCustomerId(), m.getCreatedAt(), m.getStatus(), m.getTotalAmount());
    }

    public OrderingOrders toModel() {
        return new OrderingOrders(orderId, customerId, createdAt, status, totalAmount);
    }
}

package com.example.demo_jdbc_samples.repository.springdata;

import com.example.demo_jdbc_samples.model.CustomerCustomers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** customer.customers as a Spring Data JDBC aggregate (columns: snake_case of the fields). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "customer", value = "customers")
public class CustomerCustomersEntity {
    @Id
    private Integer customerId;
    private String name;
    private Long creditLimit;
    private Long creditTotal;

    public static CustomerCustomersEntity of(CustomerCustomers m) {
        return new CustomerCustomersEntity(m.getCustomerId(), m.getName(), m.getCreditLimit(), m.getCreditTotal());
    }

    public CustomerCustomers toModel() {
        return new CustomerCustomers(customerId, name, creditLimit, creditTotal);
    }
}

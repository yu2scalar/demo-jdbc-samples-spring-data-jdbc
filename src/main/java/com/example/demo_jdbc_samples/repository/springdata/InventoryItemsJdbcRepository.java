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

public interface InventoryItemsJdbcRepository extends ListCrudRepository<InventoryItemsEntity, Integer> {

    @Lock(LockMode.PESSIMISTIC_WRITE)
    Optional<InventoryItemsEntity> findWithLockByItemId(Integer itemId);

    List<InventoryItemsEntity> findBy(Limit limit);

    List<InventoryItemsEntity> findByItemId(Integer itemId, Limit limit);

    @Modifying
    @Query("UPDATE inventory.items SET name = :name, price = :price, stock = :stock WHERE item_id = :itemId")
    int updateRow(@Param("itemId") Integer itemId, @Param("name") String name,
        @Param("price") Long price, @Param("stock") Integer stock);

    default int updateRow(InventoryItemsEntity e) {
        return updateRow(e.getItemId(), e.getName(), e.getPrice(), e.getStock());
    }

    @Modifying
    @Query("DELETE FROM inventory.items WHERE item_id = :itemId")
    int deleteRow(@Param("itemId") Integer itemId);

    // DELETE, not TRUNCATE: ScalarDB Native JDBC refuses TRUNCATE on a ScalarDB table
    @Modifying
    @Query("DELETE FROM inventory.items")
    void truncate();
}

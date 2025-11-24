package com.meli.meli_ecommerce_orders_api.repository;

import com.meli.meli_ecommerce_orders_api.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for {@link Order} entities.
 * <p>
 * This interface extends {@link JpaRepository}, providing full CRUD functionality.
 * It is the data access layer for our Order aggregate root.
 * </p>
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Finds all orders that have not been soft-deleted.
     * Spring Data JPA automatically implements this method based on its name.
     *
     * @return A list of all active (non-deleted) orders.
     */
    List<Order> findByDeletedAtIsNull();
}

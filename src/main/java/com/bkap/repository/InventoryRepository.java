package com.bkap.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkap.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minStockLevel")
    List<Inventory> findLowStockProducts();

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minStockLevel")
    Page<Inventory> findLowStockProducts(Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE LOWER(i.product.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Inventory> searchByProductName(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantity <= i.minStockLevel")
    long countLowStockProducts();
}

package com.bkap.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkap.entity.InventoryTransaction;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByProductIdOrderByCreatedDateDesc(Long productId);

    Page<InventoryTransaction> findByProductIdOrderByCreatedDateDesc(Long productId, Pageable pageable);

    Page<InventoryTransaction> findAllByOrderByCreatedDateDesc(Pageable pageable);

    @Query("SELECT it FROM InventoryTransaction it WHERE it.createdDate BETWEEN :startDate AND :endDate ORDER BY it.createdDate DESC")
    List<InventoryTransaction> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT it FROM InventoryTransaction it WHERE LOWER(it.product.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY it.createdDate DESC")
    Page<InventoryTransaction> searchByProductName(@Param("keyword") String keyword, Pageable pageable);
}

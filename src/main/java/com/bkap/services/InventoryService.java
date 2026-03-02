package com.bkap.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.bkap.entity.Inventory;
import com.bkap.entity.InventoryTransaction;

public interface InventoryService {

    List<Inventory> getAll();
    Page<Inventory> getAll(Integer pageNo);
    Optional<Inventory> findById(Long id);
    Optional<Inventory> findByProductId(Long productId);
    Boolean create(Inventory inventory);
    Boolean update(Inventory inventory);
    Boolean delete(Long id);

    List<Inventory> findLowStockProducts();
    Page<Inventory> findLowStockProducts(Integer pageNo);
    long countLowStockProducts();

    Page<Inventory> searchByProductName(String keyword, Integer pageNo);

    Boolean adjustStock(Long productId, Integer quantity, String transactionType, String note, String username);

    List<InventoryTransaction> getTransactionsByProductId(Long productId);
    Page<InventoryTransaction> getAllTransactions(Integer pageNo);
    Page<InventoryTransaction> searchTransactionsByProductName(String keyword, Integer pageNo);
}

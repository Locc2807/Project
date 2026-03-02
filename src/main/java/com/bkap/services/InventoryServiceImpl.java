package com.bkap.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.entity.Inventory;
import com.bkap.entity.InventoryTransaction;
import com.bkap.entity.Product;
import com.bkap.repository.InventoryRepository;
import com.bkap.repository.InventoryTransactionRepository;
import com.bkap.repository.ProductRepository;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

    private static final int PAGE_SIZE = 10;

    @Override
    public List<Inventory> getAll() {
        return inventoryRepository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdated"));
    }

    @Override
    public Page<Inventory> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "lastUpdated"));
        return inventoryRepository.findAll(pageable);
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return inventoryRepository.findById(id);
    }

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public Boolean create(Inventory inventory) {
        try {
            inventory.setLastUpdated(new Date());
            inventoryRepository.save(inventory);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public Boolean update(Inventory inventory) {
        try {
            inventory.setLastUpdated(new Date());
            inventoryRepository.save(inventory);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        try {
            inventoryRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Inventory> findLowStockProducts() {
        return inventoryRepository.findLowStockProducts();
    }

    @Override
    public Page<Inventory> findLowStockProducts(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, PAGE_SIZE);
        return inventoryRepository.findLowStockProducts(pageable);
    }

    @Override
    public long countLowStockProducts() {
        return inventoryRepository.countLowStockProducts();
    }

    @Override
    public Page<Inventory> searchByProductName(String keyword, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, PAGE_SIZE);
        return inventoryRepository.searchByProductName(keyword, pageable);
    }

    @Override
    @Transactional
    public Boolean adjustStock(Long productId, Integer quantity, String transactionType, String note, String username) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                return false;
            }

            Product product = productOpt.get();
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
            Inventory inventory;

            if (inventoryOpt.isPresent()) {
                inventory = inventoryOpt.get();
            } else {
                inventory = new Inventory();
                inventory.setProduct(product);
                inventory.setQuantity(0);
                inventory.setMinStockLevel(10);
            }

            InventoryTransaction.TransactionType type = InventoryTransaction.TransactionType.valueOf(transactionType);
            
            if (type == InventoryTransaction.TransactionType.IMPORT) {
                inventory.setQuantity(inventory.getQuantity() + quantity);
            } else if (type == InventoryTransaction.TransactionType.EXPORT) {
                if (inventory.getQuantity() < quantity) {
                    return false;
                }
                inventory.setQuantity(inventory.getQuantity() - quantity);
            } else if (type == InventoryTransaction.TransactionType.ADJUSTMENT) {
                inventory.setQuantity(quantity);
            }

            inventory.setLastUpdated(new Date());
            inventoryRepository.save(inventory);

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setTransactionType(type);
            transaction.setQuantity(quantity);
            transaction.setNote(note);
            transaction.setCreatedBy(username);
            transaction.setCreatedDate(new Date());
            transactionRepository.save(transaction);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<InventoryTransaction> getTransactionsByProductId(Long productId) {
        return transactionRepository.findByProductIdOrderByCreatedDateDesc(productId);
    }

    @Override
    public Page<InventoryTransaction> getAllTransactions(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, PAGE_SIZE);
        return transactionRepository.findAllByOrderByCreatedDateDesc(pageable);
    }

    @Override
    public Page<InventoryTransaction> searchTransactionsByProductName(String keyword, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, PAGE_SIZE);
        return transactionRepository.searchByProductName(keyword, pageable);
    }
}

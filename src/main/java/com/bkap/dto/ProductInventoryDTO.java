package com.bkap.dto;

import com.bkap.entity.Inventory;
import com.bkap.entity.Product;

public class ProductInventoryDTO {
    private Product product;
    private Integer stockQuantity;
    private boolean inStock;
    private boolean lowStock;

    public ProductInventoryDTO(Product product, Inventory inventory) {
        this.product = product;
        if (inventory != null) {
            this.stockQuantity = inventory.getQuantity();
            this.inStock = inventory.getQuantity() > 0;
            this.lowStock = inventory.isLowStock();
        } else {
            this.stockQuantity = 0;
            this.inStock = false;
            this.lowStock = false;
        }
    }

    // Getters and Setters
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public void setLowStock(boolean lowStock) {
        this.lowStock = lowStock;
    }
}

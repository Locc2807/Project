package com.bkap.dto;

import com.bkap.entity.Inventory;
import com.bkap.entity.Product;

/**
 * DTO để hiển thị sản phẩm kèm thông tin tồn kho
 */
public class ProductWithInventoryDTO {
    
    private Product product;
    private Inventory inventory;
    private String stockStatus; // "available", "low", "out"
    private String stockBadgeClass; // CSS class cho badge
    
    public ProductWithInventoryDTO(Product product, Inventory inventory) {
        this.product = product;
        this.inventory = inventory;
        this.stockStatus = calculateStockStatus();
        this.stockBadgeClass = calculateBadgeClass();
    }
    
    private String calculateStockStatus() {
        if (inventory == null || inventory.getQuantity() == null || inventory.getQuantity() == 0) {
            return "out";
        }
        if (inventory.isLowStock()) {
            return "low";
        }
        return "available";
    }
    
    private String calculateBadgeClass() {
        return switch (stockStatus) {
            case "available" -> "bg-green-100 text-green-800";
            case "low" -> "bg-yellow-100 text-yellow-800";
            case "out" -> "bg-red-100 text-red-800";
            default -> "bg-gray-100 text-gray-800";
        };
    }
    
    public String getStockStatusText() {
        return switch (stockStatus) {
            case "available" -> "Đủ hàng";
            case "low" -> "Sắp hết";
            case "out" -> "Hết hàng";
            default -> "N/A";
        };
    }
    
    public Integer getQuantity() {
        return inventory != null ? inventory.getQuantity() : 0;
    }
    
    public Integer getMinStockLevel() {
        return inventory != null ? inventory.getMinStockLevel() : 0;
    }

    public String getStockDisplay() {
        if (inventory == null || inventory.getQuantity() == null) {
            return "0";
        }
        return inventory.getQuantity().toString();
    }
    
    public String getStockDisplayWithStatus() {
        if (inventory == null || inventory.getQuantity() == null) {
            return "0 - Hết hàng";
        }
        return inventory.getQuantity() + " - " + getStockStatusText();
    }
    
    // Getters and Setters
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        this.stockStatus = calculateStockStatus();
        this.stockBadgeClass = calculateBadgeClass();
    }
    
    public String getStockStatus() {
        return stockStatus;
    }
    
    public String getStockBadgeClass() {
        return stockBadgeClass;
    }
}

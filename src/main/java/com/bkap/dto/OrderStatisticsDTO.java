package com.bkap.dto;

import java.time.LocalDate;

/**
 * DTO cho thống kê đơn hàng theo ngày/tháng
 */
public class OrderStatisticsDTO {
    
    private LocalDate date;
    private Long orderCount;
    private Double totalRevenue;
    private Long completedOrders;
    private Long cancelledOrders;

    public OrderStatisticsDTO() {
    }

    public OrderStatisticsDTO(LocalDate date, Long orderCount, Double totalRevenue) {
        this.date = date;
        this.orderCount = orderCount;
        this.totalRevenue = totalRevenue;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }
}

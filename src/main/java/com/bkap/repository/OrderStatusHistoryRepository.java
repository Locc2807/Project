package com.bkap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.entity.OrderStatusHistory;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    
    /**
     * Tìm lịch sử thay đổi trạng thái theo order ID
     */
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);
    
    /**
     * Tìm lịch sử theo người thay đổi
     */
    List<OrderStatusHistory> findByChangedByOrderByChangedAtDesc(String changedBy);
    
    /**
     * Đếm số lần thay đổi trạng thái của một đơn hàng
     */
    long countByOrderId(Long orderId);
}

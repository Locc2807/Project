package com.bkap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bkap.entity.OrderNote;

@Repository
public interface OrderNoteRepository extends JpaRepository<OrderNote, Long> {
    
    /**
     * Tìm tất cả ghi chú của một đơn hàng
     */
    List<OrderNote> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    /**
     * Tìm ghi chú nội bộ của một đơn hàng
     */
    List<OrderNote> findByOrderIdAndIsInternalOrderByCreatedAtDesc(Long orderId, Boolean isInternal);
    
    /**
     * Tìm ghi chú theo người tạo
     */
    List<OrderNote> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    
    /**
     * Đếm số ghi chú của một đơn hàng
     */
    long countByOrderId(Long orderId);
}

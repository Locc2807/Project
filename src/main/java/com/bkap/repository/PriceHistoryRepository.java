package com.bkap.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bkap.entity.PriceHistory;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    // Lấy lịch sử giá của một sản phẩm
    List<PriceHistory> findByProductIdOrderByChangedAtDesc(Long productId);
    
    Page<PriceHistory> findByProductIdOrderByChangedAtDesc(Long productId, Pageable pageable);

    // Lấy lịch sử giá gần nhất của sản phẩm
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.product.id = :productId ORDER BY ph.changedAt DESC")
    List<PriceHistory> findLatestByProductId(@Param("productId") Long productId, Pageable pageable);

    // Lấy tất cả lịch sử giá (phân trang)
    Page<PriceHistory> findAllByOrderByChangedAtDesc(Pageable pageable);

    // Đếm số lần thay đổi giá của sản phẩm
    long countByProductId(Long productId);

    // Tìm các thay đổi giá lớn (> threshold %)
    @Query("SELECT ph FROM PriceHistory ph WHERE ABS(ph.changePercentage) >= :threshold ORDER BY ph.changedAt DESC")
    List<PriceHistory> findLargePriceChanges(@Param("threshold") Double threshold);
}

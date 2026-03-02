package com.bkap.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkap.entity.OrderDetail;
import com.bkap.entity.Product;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);
    
    // Tìm sản phẩm bán chạy nhất (tất cả danh mục)
    @Query("SELECT od.product FROM OrderDetail od " +
           "GROUP BY od.product " +
           "ORDER BY SUM(od.quantity) DESC")
    List<Product> findTopSellingProducts(Pageable pageable);
    
    // Tìm sản phẩm bán chạy nhất theo danh mục
    @Query("SELECT od.product FROM OrderDetail od " +
           "WHERE LOWER(od.product.category.name) = LOWER(:categoryName) " +
           "GROUP BY od.product " +
           "ORDER BY SUM(od.quantity) DESC")
    List<Product> findTopSellingProductsByCategory(@Param("categoryName") String categoryName, Pageable pageable);
    
    // Tính tổng số lượng đã bán của một sản phẩm
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od WHERE od.product.id = :productId")
    Long getTotalSoldQuantity(@Param("productId") Long productId);
    
    // Đếm số đơn hàng đang xử lý có chứa sản phẩm này (status 1-4)
    @Query("SELECT COUNT(DISTINCT od.order.id) FROM OrderDetail od " +
           "WHERE od.product.id = :productId " +
           "AND od.order.status BETWEEN 1 AND 4")
    long countPendingOrdersByProduct(@Param("productId") Long productId);
}

package com.bkap.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.bkap.entity.PriceHistory;
import com.bkap.entity.Product;

public interface PriceHistoryService {

    // Lưu lịch sử thay đổi giá
    PriceHistory create(PriceHistory priceHistory);

    // Lưu lịch sử khi cập nhật giá sản phẩm
    PriceHistory logPriceChange(Product product, Double oldPrice, Double newPrice, 
                                String changedBy, String changeReason);

    // Lấy lịch sử giá của sản phẩm
    List<PriceHistory> getByProductId(Long productId);
    
    Page<PriceHistory> getByProductId(Long productId, Integer pageNo);

    // Lấy lịch sử giá gần nhất
    PriceHistory getLatestByProductId(Long productId);

    // Lấy tất cả lịch sử (phân trang)
    Page<PriceHistory> getAll(Integer pageNo);

    // Đếm số lần thay đổi giá
    long countByProductId(Long productId);

    // Kiểm tra xem có thay đổi giá lớn không (> threshold %)
    boolean isLargePriceChange(Double oldPrice, Double newPrice, Double threshold);

    // Tính phần trăm thay đổi
    Double calculateChangePercentage(Double oldPrice, Double newPrice);
}

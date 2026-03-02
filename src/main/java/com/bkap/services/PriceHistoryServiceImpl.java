package com.bkap.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.entity.PriceHistory;
import com.bkap.entity.Product;
import com.bkap.repository.PriceHistoryRepository;

@Service
public class PriceHistoryServiceImpl implements PriceHistoryService {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Override
    @Transactional
    public PriceHistory create(PriceHistory priceHistory) {
        if (priceHistory.getChangedAt() == null) {
            priceHistory.setChangedAt(new Date());
        }
        return priceHistoryRepository.save(priceHistory);
    }

    @Override
    @Transactional
    public PriceHistory logPriceChange(Product product, Double oldPrice, Double newPrice, 
                                       String changedBy, String changeReason) {
        PriceHistory history = new PriceHistory();
        history.setProduct(product);
        history.setOldPrice(oldPrice);
        history.setNewPrice(newPrice);
        history.setChangedBy(changedBy);
        history.setChangeReason(changeReason);
        history.setChangedAt(new Date());
        
        // Trigger trong database sẽ tự động tính changePercentage
        // Nhưng ta cũng tính ở đây để có thể sử dụng trong logic
        if (oldPrice != null && oldPrice > 0) {
            Double percentage = ((newPrice - oldPrice) / oldPrice) * 100;
            history.setChangePercentage(Math.round(percentage * 100.0) / 100.0);
        }
        
        return priceHistoryRepository.save(history);
    }

    @Override
    public List<PriceHistory> getByProductId(Long productId) {
        return priceHistoryRepository.findByProductIdOrderByChangedAtDesc(productId);
    }

    @Override
    public Page<PriceHistory> getByProductId(Long productId, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 10);
        return priceHistoryRepository.findByProductIdOrderByChangedAtDesc(productId, pageable);
    }

    @Override
    public PriceHistory getLatestByProductId(Long productId) {
        Pageable pageable = PageRequest.of(0, 1);
        List<PriceHistory> histories = priceHistoryRepository.findLatestByProductId(productId, pageable);
        return histories.isEmpty() ? null : histories.get(0);
    }

    @Override
    public Page<PriceHistory> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 20);
        return priceHistoryRepository.findAllByOrderByChangedAtDesc(pageable);
    }

    @Override
    public long countByProductId(Long productId) {
        return priceHistoryRepository.countByProductId(productId);
    }

    @Override
    public boolean isLargePriceChange(Double oldPrice, Double newPrice, Double threshold) {
        if (oldPrice == null || oldPrice == 0) {
            return false;
        }
        Double percentage = Math.abs(calculateChangePercentage(oldPrice, newPrice));
        return percentage >= threshold;
    }

    @Override
    public Double calculateChangePercentage(Double oldPrice, Double newPrice) {
        if (oldPrice == null || oldPrice == 0) {
            return null;
        }
        return Math.round(((newPrice - oldPrice) / oldPrice) * 100 * 100.0) / 100.0;
    }
}

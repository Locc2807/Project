package com.bkap.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bkap.entity.Review;

public interface ReviewService {
	List<Review> findByCustomerID(Long customerId);
	List<Review> getReviewsByProductId(Long productId);
	Boolean save(Review review);
	Page<Review> findAll(String keyword, Pageable pageable);
	List<Review> findByProductId(Long productId);

}

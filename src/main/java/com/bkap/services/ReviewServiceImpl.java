package com.bkap.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bkap.entity.Review;
import com.bkap.repository.ReviewRepository;

@Service
public class ReviewServiceImpl implements ReviewService {
	@Autowired
	private ReviewRepository reviewRepository;

	@Override
	public List<Review> findByCustomerID(Long customerId) {
		// TODO Auto-generated method stub
		return reviewRepository.findByCustomer_Id(customerId);
	}

	@Override
	public List<Review> getReviewsByProductId(Long productId) {
		// TODO Auto-generated method stub
		return reviewRepository.findByProductId(productId);
	}

	@Override
	public Boolean save(Review review) {
		try {
			reviewRepository.save(review);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Page<Review> findAll(String keyword, Pageable pageable) {
	    if (keyword != null && !keyword.isEmpty()) {
	        return reviewRepository.searchByContentOrCustomerName(keyword, pageable);
	    } else {
	        return reviewRepository.findAll(pageable);
	    }
	}

	@Override
	public List<Review> findByProductId(Long productId) {
		// TODO Auto-generated method stub
		return reviewRepository.findByProductId(productId);
	}

}

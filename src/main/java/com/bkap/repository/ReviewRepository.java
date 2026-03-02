package com.bkap.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkap.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByCustomer_Id(Long customerId);

	List<Review> findByProductId(Long productId);

	@Query("SELECT r FROM Review r WHERE LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	Page<Review> searchByContentOrCustomerName(@Param("keyword") String keyword, Pageable pageable);

}

package com.bkap.repository;

import com.bkap.entity.Customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	List<Customer> findTop5ByOrderByCreatedDesc(); // Lấy 5 khách mới nhất

	Page<Customer> findAll(Pageable pageable);
	
	 Customer findByEmail(String email);

	@Query("SELECT c FROM Customer c WHERE " + "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " + "c.phone LIKE CONCAT('%', :keyword, '%')")
	Page<Customer> searchByNameEmailOrPhone(@Param("keyword") String keyword, Pageable pageable);

}

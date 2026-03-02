package com.bkap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkap.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer>{
	List<Category> findAllByOrderByIdAsc();
	
	List<Category> findAllByStatusTrueOrderByIdAsc();
	
	@Query("SELECT c FROM Category c WHERE c.name LIKE CONCAT('%', :keyword, '%')")
	List<Category> searchCategory(@Param("keyword") String keyword);

}

package com.bkap.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.bkap.entity.Category;

public interface CategoryService {
	List<Category> getAll();
	Boolean create(Category category);
	Category findById(Integer id);
	Boolean update(Category category);
	Boolean delete(Integer id);
	List<Category> searchCategory(String keyword);
	
	List<Category> getActiveCategories(); 
	
	Page<Category> getAll(Integer pageNo);
	Page<Category> searchCategory(String keyword, Integer pageNo);
}

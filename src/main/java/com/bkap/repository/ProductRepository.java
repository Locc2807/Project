package com.bkap.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkap.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // ==== Basic Queries ====
    List<Product> findByCategory_NameIgnoreCase(String categoryName);
    Page<Product> findByCategory_NameIgnoreCase(String categoryName, Pageable pageable);
    Page<Product> findByCategory_NameIgnoreCaseAndBrandIn(String categoryName, List<String> brands, Pageable pageable);
    List<Product> findByCategory_NameIgnoreCaseAndBrandIn(String category, List<String> brands);
    Page<Product> findByCategoryId(int categoryId, Pageable pageable);
    List<Product> findTop4ByCategoryIdAndIdNot(Long categoryId, Long id);

    // ==== Full-text Search ====
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProduct(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByNameOrCategory(@Param("keyword") String keyword, Pageable pageable);

    // ==== Top / Latest Products ====
    @Query("SELECT p FROM Product p WHERE LOWER(p.category.name) = 'laptops' ORDER BY p.id DESC")
    List<Product> findTop3LatestLaptops(Pageable pageable);

    List<Product> findTop3ByCategory_NameIgnoreCaseOrderByIdDesc(String categoryName); // smartphones, accessories, cameras
}

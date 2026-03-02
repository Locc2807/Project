package com.bkap.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bkap.entity.Product;

public interface ProductService {

    // ==== Basic CRUD ====
    List<Product> getAll();
    List<Product> findAll();
    Boolean create(Product product);
    Boolean update(Product product);
    Boolean delete(Long id);
    Optional<Product> findById(long id);
    long countAll();

    // ==== Search & Filter ====
    List<Product> searchProduct(String keyword);
    Page<Product> searchByNameOrCategory(String keyword, Integer pageNo);
    Page<Product> searchCategory(String keyword, Integer pageNo);
    List<Product> findRelatedProducts(Long categoryId, Long excludeId);

    // ==== Pagination ====
    Page<Product> getAll(Integer pageNo);
    Page<Product> getLaptopsByPage(int pageNo, int pageSize);

    // ==== General by Category ====
    List<Product> getByCategoryName(String categoryName);
    List<Product> findByCategoryAndBrands(String category, List<String> brands);

    // ==== Newest Products ====
    List<Product> findTop3Latest();
    List<Product> findTop3LatestByCategory(String categoryName); // Smartphones
    List<Product> findTop3LatestCamerasByCategory(String categoryName);
    List<Product> findTop3LatestAccessoriesByCategory(String categoryName);
    
    // ==== Top Selling Products (based on actual sales) ====
    List<Product> findTop3BestSelling();
    List<Product> findTop3BestSellingByCategory(String categoryName);

    // ==== Laptops ====
    Page<Product> findLaptopsWithPageable(Pageable pageable);
    Page<Product> findLaptopsByBrandsWithPageable(List<String> brands, Pageable pageable);
    Page<Product> findByCategoryIdWithPageable(int categoryId, Pageable pageable);
    Page<Product> findLaptopsByBrands(List<String> brands, int pageNo, int pageSize);

    // ==== Smartphones ====
    Page<Product> findSmartphonesWithPageable(Pageable pageable);
    Page<Product> findSmartphonesByBrandsWithPageable(List<String> brands, Pageable pageable);
    Page<Product> findSmartphonesByBrands(List<String> brands, int pageNo, int pageSize);

    // ==== Cameras ====
    Page<Product> findCamerasWithPageable(Pageable pageable);
    Page<Product> findCamerasByBrandsWithPageable(List<String> brands, Pageable pageable);
    Page<Product> findCamerasByBrands(List<String> brands, int pageNo, int pageSize);
    List<Product> findCamerasByBrands(String category, List<String> brands);

    // ==== Accessories ====
    Page<Product> findAccessoriesWithPageable(Pageable pageable);
    Page<Product> findAccessoriesByBrandsWithPageable(List<String> brands, Pageable pageable);
    Page<Product> findAccessoriesByBrands(List<String> brands, int pageNo, int pageSize);
    List<Product> findAccessoriesByBrands(String category, List<String> brands);
    
    // ==== New methods for improvements ====
    Product duplicate(Long productId);
    List<Product> bulkUpdateStatus(List<Long> productIds, Boolean status);
    List<Product> bulkDelete(List<Long> productIds);
}

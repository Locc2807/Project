package com.bkap.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.entity.Category;
import com.bkap.entity.Inventory;
import com.bkap.entity.Product;
import com.bkap.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private com.bkap.repository.OrderDetailRepository orderDetailRepository;
	
	@Autowired
	private InventoryService inventoryService;

	@Override
	public List<Product> getAll() {
		// TODO Auto-generated method stub
		return this.productRepository.findAll();
	}

	@Override
	@Transactional
	public Boolean create(Product product) {
		try {
			// Lưu sản phẩm
			Product savedProduct = this.productRepository.save(product);
			
			// Tự động tạo Inventory với quantity = 0
			Inventory inventory = new Inventory();
			inventory.setProduct(savedProduct);
			inventory.setQuantity(0);
			inventory.setMinStockLevel(10); // Mặc định ngưỡng cảnh báo là 10
			inventory.setLastUpdated(new Date());
			inventoryService.create(inventory);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Optional<Product> findById(long id) {
		return productRepository.findById(id);
	}

	@Override
	public Boolean update(Product product) {
		try {
			this.productRepository.save(product);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Boolean delete(Long id) {
		try {
			Product product = findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
			
			// Kiểm tra tồn kho
			Optional<Inventory> inventoryOpt = inventoryService.findByProductId(id);
			if (inventoryOpt.isPresent() && inventoryOpt.get().getQuantity() > 0) {
				throw new RuntimeException("Không thể xóa sản phẩm còn " + 
					inventoryOpt.get().getQuantity() + " trong kho!");
			}
			
			// Kiểm tra đơn hàng đang xử lý (status 1-4)
			long pendingOrders = orderDetailRepository.countPendingOrdersByProduct(id);
			if (pendingOrders > 0) {
				throw new RuntimeException("Không thể xóa sản phẩm có " + 
					pendingOrders + " đơn hàng đang xử lý!");
			}
			
			this.productRepository.delete(product);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<Product> searchProduct(String keyword) {
		// TODO Auto-generated method stub
		return this.productRepository.searchProduct(keyword);
	}

	@Override
	public Page<Product> getAll(Integer pageNo) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(pageNo - 1, 2);
		return this.productRepository.findAll(pageable);
	}

	@Override
	public Page<Product> searchCategory(String keyword, Integer pageNo) {
		List list = this.searchProduct(keyword);

		Pageable pageable = PageRequest.of(pageNo - 1, 2);

		Integer start = (int) pageable.getOffset();
		Integer end = (int) ((pageable.getOffset() + pageable.getPageSize()) > list.size() ? list.size()
				: pageable.getOffset() + pageable.getPageSize());

		list = list.subList(start, end);
		return new PageImpl<Product>(list, pageable, this.searchProduct(keyword).size());
	}

	@Override
	public List<Product> getByCategoryName(String categoryName) {
		// TODO Auto-generated method stub
		return productRepository.findByCategory_NameIgnoreCase(categoryName);
	}

	@Override
	public Page<Product> searchByNameOrCategory(String keyword, Integer pageNo) {
		Pageable pageable = PageRequest.of(pageNo - 1, 2);
		return this.productRepository.searchByNameOrCategory(keyword, pageable);
	}

	@Override
	public Page<Product> findLaptopsByBrands(List<String> brands, int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize); // KHÔNG trừ -1 nữa
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn("laptops", brands, pageable);
	}

	@Override
	public Page<Product> getLaptopsByPage(int pageNo, int pageSize) {
		// pageNo đã là pageIndex (tức là đã -1 từ controller)
		Pageable pageable = PageRequest.of(pageNo, pageSize); // KHÔNG trừ -1 nữa
		return productRepository.findByCategory_NameIgnoreCase("laptops", pageable);
	}

	@Override
	public List<Product> findTop3Latest() {
		Pageable pageable = PageRequest.of(0, 3);
		return productRepository.findTop3LatestLaptops(pageable);
	}

	@Override
	public Page<Product> findLaptopsByBrandsWithPageable(List<String> brands, Pageable pageable) {
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn("laptops", brands, pageable);
	}

	@Override
	public Page<Product> findLaptopsWithPageable(Pageable pageable) {
		return productRepository.findByCategory_NameIgnoreCase("laptops", pageable);
	}

	@Override
	public Page<Product> findByCategoryIdWithPageable(int categoryId, Pageable pageable) {
		return productRepository.findByCategoryId(categoryId, pageable);
	}

	@Override
	public Page<Product> findSmartphonesWithPageable(Pageable pageable) {
		return productRepository.findByCategory_NameIgnoreCase("smartphones", pageable);
	}

	@Override
	public Page<Product> findSmartphonesByBrandsWithPageable(List<String> brands, Pageable pageable) {
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn("smartphones", brands, pageable);
	}

	@Override
	public List<Product> findTop3LatestByCategory(String categoryName) {
		// TODO Auto-generated method stub
		return productRepository.findTop3ByCategory_NameIgnoreCaseOrderByIdDesc(categoryName);
	}

	@Override
	public Page<Product> findSmartphonesByBrands(List<String> brands, int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize); // KHÔNG trừ -1 nữa
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn("smartphones", brands, pageable);
	}

	@Override
	public List<Product> findByCategoryAndBrands(String category, List<String> brands) {
		// TODO Auto-generated method stub
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn(category, brands);
	}

	// Cameras
	@Override
	public Page<Product> findCamerasWithPageable(Pageable pageable) {
		// TODO Auto-generated method stub
		return productRepository.findByCategory_NameIgnoreCase("cameras", pageable);
	}

	@Override
	public Page<Product> findCamerasByBrandsWithPageable(List<String> brands, Pageable pageable) {
		// TODO Auto-generated method stub
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn("cameras", brands, pageable);
	}

	@Override
	public List<Product> findTop3LatestCamerasByCategory(String categoryName) {
		// TODO Auto-generated method stub
		return productRepository.findTop3ByCategory_NameIgnoreCaseOrderByIdDesc(categoryName);
	}

	@Override
	public Page<Product> findCamerasByBrands(List<String> brands, int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize); // KHÔNG trừ -1 nữa
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn("cameras", brands, pageable);
	}

	@Override
	public List<Product> findCamerasByBrands(String category, List<String> brands) {
		// TODO Auto-generated method stub
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn(category, brands);
	}

	// Accessories
	@Override
	public Page<Product> findAccessoriesWithPageable(Pageable pageable) {
		// TODO Auto-generated method stub
		return productRepository.findByCategory_NameIgnoreCase("accessories", pageable);
	}

	@Override
	public Page<Product> findAccessoriesByBrandsWithPageable(List<String> brands, Pageable pageable) {
		// TODO Auto-generated method stub
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn("accessories", brands, pageable);
	}

	@Override
	public List<Product> findTop3LatestAccessoriesByCategory(String categoryName) {
		// TODO Auto-generated method stub
		return productRepository.findTop3ByCategory_NameIgnoreCaseOrderByIdDesc(categoryName);
	}

	@Override
	public Page<Product> findAccessoriesByBrands(List<String> brands, int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize); // KHÔNG trừ -1 nữa
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn("accessories", brands, pageable);
	}

	@Override
	public List<Product> findAccessoriesByBrands(String category, List<String> brands) {
		// TODO Auto-generated method stub
		return productRepository.findByCategory_NameIgnoreCaseAndBrandIn(category, brands);
	}

	// Dashboard
	@Override
	public long countAll() {
		// TODO Auto-generated method stub
		return productRepository.count();
	}

	// Hot Deal
	@Override
	public List<Product> findAll() {
		// TODO Auto-generated method stub
		return productRepository.findAll();
	}

	@Override
	public List<Product> findRelatedProducts(Long categoryId, Long excludeId) {
		// TODO Auto-generated method stub
		return productRepository.findTop4ByCategoryIdAndIdNot(categoryId, excludeId);
	}

	// ==== Top Selling Products (based on actual sales) ====
	
	@Override
	public List<Product> findTop3BestSelling() {
		Pageable pageable = PageRequest.of(0, 3);
		List<Product> bestSelling = orderDetailRepository.findTopSellingProducts(pageable);
		
		// Fallback: Nếu chưa có đơn hàng nào, trả về sản phẩm mới nhất
		if (bestSelling.isEmpty()) {
			return findTop3Latest();
		}
		
		return bestSelling;
	}
	
	@Override
	public List<Product> findTop3BestSellingByCategory(String categoryName) {
		Pageable pageable = PageRequest.of(0, 3);
		List<Product> bestSelling = orderDetailRepository.findTopSellingProductsByCategory(categoryName, pageable);
		
		// Fallback: Nếu chưa có đơn hàng nào, trả về sản phẩm mới nhất của category
		if (bestSelling.isEmpty()) {
			return findTop3LatestByCategory(categoryName);
		}
		
		return bestSelling;
	}

	
	// ==== New methods for improvements ====
	
	@Override
	@Transactional
	public Product duplicate(Long productId) {
		Product original = findById(productId)
			.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
		
		// Tạo sản phẩm mới
		Product duplicate = new Product();
		duplicate.setName(original.getName() + " - Copy");
		duplicate.setPrice(original.getPrice());
		duplicate.setCategory(original.getCategory());
		duplicate.setBrand(original.getBrand());
		duplicate.setDescription(original.getDescription());
		duplicate.setImage(original.getImage());
		duplicate.setStatus(false); // Mặc định tắt
		duplicate.setCreated(new Date());
		
		// Lưu sản phẩm (sẽ tự động tạo Inventory nhờ method create)
		Product saved = productRepository.save(duplicate);
		
		// Tạo Inventory với quantity = 0
		Inventory inventory = new Inventory();
		inventory.setProduct(saved);
		inventory.setQuantity(0);
		inventory.setMinStockLevel(10);
		inventory.setLastUpdated(new Date());
		inventoryService.create(inventory);
		
		return saved;
	}
	
	@Override
	@Transactional
	public List<Product> bulkUpdateStatus(List<Long> productIds, Boolean status) {
		List<Product> products = productRepository.findAllById(productIds);
		for (Product product : products) {
			product.setStatus(status);
		}
		return productRepository.saveAll(products);
	}
	
	@Override
	@Transactional
	public List<Product> bulkDelete(List<Long> productIds) {
		List<Product> deletedProducts = new java.util.ArrayList<>();
		for (Long id : productIds) {
			try {
				if (delete(id)) {
					findById(id).ifPresent(deletedProducts::add);
				}
			} catch (Exception e) {
				// Log lỗi nhưng tiếp tục xóa các sản phẩm khác
				System.err.println("Lỗi khi xóa sản phẩm #" + id + ": " + e.getMessage());
			}
		}
		return deletedProducts;
	}


	@Autowired
	private PriceHistoryService priceHistoryService;
}

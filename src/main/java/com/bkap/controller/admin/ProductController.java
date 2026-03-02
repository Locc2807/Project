package com.bkap.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bkap.dto.ProductDTO;
import com.bkap.entity.Category;
import com.bkap.entity.Product;
import com.bkap.services.CategoryService;
import com.bkap.services.ProductService;
import com.bkap.services.StorageService;

@Controller
public class ProductController {
	@Autowired
	private StorageService storageService;

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@GetMapping("admin/product")
		public String index(Model model, 
				@Param("keyword") String keyword,
				@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
				@RequestParam(name = "stockStatus", required = false) String stockStatus) {

			Page<Product> listProduct;

			if (keyword != null && !keyword.isEmpty()) {
				listProduct = this.productService.searchByNameOrCategory(keyword, pageNo);
				model.addAttribute("keyword", keyword);
			} else {
				listProduct = this.productService.getAll(pageNo);
			}

			// Tạo danh sách ProductWithInventoryDTO
			List<com.bkap.dto.ProductWithInventoryDTO> productsWithInventory = new java.util.ArrayList<>();
			for (Product product : listProduct.getContent()) {
				Optional<com.bkap.entity.Inventory> inventoryOpt = 
					inventoryService.findByProductId(product.getId());

				com.bkap.dto.ProductWithInventoryDTO dto = 
					new com.bkap.dto.ProductWithInventoryDTO(product, inventoryOpt.orElse(null));

				// Lọc theo stock status nếu có
				if (stockStatus != null && !stockStatus.isEmpty()) {
					if (stockStatus.equals(dto.getStockStatus())) {
						productsWithInventory.add(dto);
					}
				} else {
					productsWithInventory.add(dto);
				}
			}

			model.addAttribute("totalPage", listProduct.getTotalPages());
			model.addAttribute("currentPage", pageNo);
			model.addAttribute("listProduct", productsWithInventory);
			model.addAttribute("stockStatus", stockStatus);
			return "admin/product/index";
		}

	@GetMapping("admin/add-product")
	public String add(Model model) {

		Product product = new Product();
		model.addAttribute("product", product);

		List<Category> listCate = this.categoryService.getAll();
		model.addAttribute("listCate", listCate);
		return "admin/product/add";
	}

	@PostMapping("admin/add-product")
	public String save(@ModelAttribute("product") Product product, @RequestParam("imageFile") MultipartFile file,
			RedirectAttributes redirect) {
		// upload file
		String fileName = this.storageService.store(file);
		product.setImage(fileName);

		if (productService.create(product)) {
			redirect.addFlashAttribute("success", "Thêm sản phẩm thành công");
			return "redirect:/admin/category";
		} else {
			redirect.addFlashAttribute("error", "Thêm thất bại");
			return "redirect:/admin/add-product";
		}
	}

	@GetMapping("admin/edit-product/{id}")
		public String edit(Model model, @PathVariable("id") Long id) {
			Product product = this.productService.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

			List<Category> listCate = this.categoryService.getAll();

			if (product.getCategory() == null) {
				product.setCategory(new Category());
			}

			// Lấy thông tin inventory
			Optional<com.bkap.entity.Inventory> inventoryOpt = inventoryService.findByProductId(id);

			model.addAttribute("product", product);
			model.addAttribute("listCate", listCate);
			model.addAttribute("inventory", inventoryOpt.orElse(null));
			return "admin/product/edit";
		}

	@PostMapping("admin/edit-product")
	public String update(@ModelAttribute("product") Product product, @RequestParam("imageFile") MultipartFile file,
			RedirectAttributes redirect) {

		Product oldProduct = productService.findById(product.getId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		// Nếu có file mới → lưu file
		if (!file.isEmpty()) {
			String filename = storageService.store(file);
			product.setImage(filename);
		} else {
			// Không upload ảnh mới → giữ lại ảnh cũ
			product.setImage(oldProduct.getImage());
		}

		if (productService.update(product)) {
			redirect.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
			return "redirect:/admin/product";
		} else {
			redirect.addFlashAttribute("error", "Cập nhật thất bại");
			return "redirect:/admin/edit-product/" + product.getId();
		}
	}

	@GetMapping("admin/delete-product/{id}")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirect) {
		if (productService.delete(id)) {
			redirect.addFlashAttribute("success", "Xóa sản phẩm thành công");
		} else {
			redirect.addFlashAttribute("error", "Xóa sản phẩm thất bại");
		}
		return "redirect:/admin/product";
	}

	@GetMapping("/api/admin/product/{id}")
	@ResponseBody
	public ResponseEntity<?> getProductById(@PathVariable("id") Long id) {
		Optional<Product> productOpt = productService.findById(id);
		if (productOpt.isPresent()) {
			Product p = productOpt.get();
			return ResponseEntity.ok(new ProductDTO(p));
		} else {
			return ResponseEntity.status(404).body("{\"message\": \"Không tìm thấy sản phẩm\"}");
		}
	}

	@Autowired
	private com.bkap.services.InventoryService inventoryService;


	// ==== NEW ENDPOINTS FOR IMPROVEMENTS ====

	@PostMapping("admin/product/duplicate/{id}")
	public String duplicate(@PathVariable("id") Long id, RedirectAttributes redirect) {
		try {
			Product duplicated = productService.duplicate(id);
			redirect.addFlashAttribute("success", "Đã nhân bản sản phẩm: " + duplicated.getName());
		} catch (Exception e) {
			redirect.addFlashAttribute("error", "Lỗi khi nhân bản: " + e.getMessage());
		}
		return "redirect:/admin/product";
	}

	@PostMapping("admin/product/bulk-update-status")
	public String bulkUpdateStatus(
			@RequestParam("productIds") List<Long> productIds,
			@RequestParam("status") Boolean status,
			RedirectAttributes redirect) {
		try {
			List<Product> updated = productService.bulkUpdateStatus(productIds, status);
			redirect.addFlashAttribute("success",
				"Đã cập nhật trạng thái " + updated.size() + " sản phẩm");
		} catch (Exception e) {
			redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/product";
	}

	@PostMapping("admin/product/bulk-delete")
	public String bulkDelete(
			@RequestParam("productIds") List<Long> productIds,
			RedirectAttributes redirect) {
		try {
			List<Product> deleted = productService.bulkDelete(productIds);
			redirect.addFlashAttribute("success",
				"Đã xóa " + deleted.size() + " sản phẩm");
		} catch (Exception e) {
			redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/product";
	}

	@GetMapping("admin/product/quick-stock/{id}")
	public String quickStock(@PathVariable("id") Long id, Model model) {
		Product product = productService.findById(id)
			.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		Optional<com.bkap.entity.Inventory> inventoryOpt = inventoryService.findByProductId(id);

		model.addAttribute("product", product);
		model.addAttribute("inventory", inventoryOpt.orElse(null));
		return "admin/product/quick-stock";
	}

	@PostMapping("admin/product/quick-stock/{id}")
	public String quickStockSubmit(
			@PathVariable("id") Long id,
			@RequestParam("quantity") Integer quantity,
			@RequestParam("note") String note,
			RedirectAttributes redirect) {
		try {
			inventoryService.adjustStock(id, quantity, "IN", note, "admin");
			redirect.addFlashAttribute("success", "Đã nhập " + quantity + " sản phẩm vào kho");
		} catch (Exception e) {
			redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/product";
	}


	@Autowired
	private com.bkap.services.PriceHistoryService priceHistoryService;

	@GetMapping("admin/product/{id}/price-history")
	public String viewPriceHistory(@PathVariable("id") Long id, Model model,
			@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo) {
		Product product = productService.findById(id)
			.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		Page<com.bkap.entity.PriceHistory> priceHistories =
			priceHistoryService.getByProductId(id, pageNo);

		model.addAttribute("product", product);
		model.addAttribute("priceHistories", priceHistories);
		model.addAttribute("totalPage", priceHistories.getTotalPages());
		model.addAttribute("currentPage", pageNo);

		return "admin/product/price-history";
	}
}

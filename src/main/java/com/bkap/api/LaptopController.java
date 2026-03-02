package com.bkap.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bkap.dto.ProductDTO;
import com.bkap.entity.Customer;
import com.bkap.entity.Product;
import com.bkap.entity.Review;
import com.bkap.services.CustomerService;
import com.bkap.services.ProductService;
import com.bkap.services.ReviewService;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LaptopController {
	@Autowired
	private CustomerService customerService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ReviewService reviewService;

	// Product Detail Page
	@GetMapping("/product/{id}")
	public String productDetail(@PathVariable("id") Long id, Model model) {
		Product p = productService.findById(id).orElse(null);
		if (p == null)
			return "redirect:/";

		ProductDTO dto = new ProductDTO(p);
		model.addAttribute("product", dto);
		model.addAttribute("reviews", reviewService.findByProductId(id));

		// Sản phẩm liên quan
		Long categoryId = p.getCategory() != null ? p.getCategory().getId().longValue() : null;
		List<Product> related = (categoryId != null) ? productService.findRelatedProducts(categoryId, p.getId())
				: List.of();
		model.addAttribute("relatedProducts", related);

		return "quickview";
	}

	@GetMapping("/product/quickview/{id}")
	public String quickViewLaptop(@PathVariable("id") Long id, Model model) {
		Product p = productService.findById(id).orElse(null);
		if (p == null)
			return "redirect:/laptops";

		ProductDTO dto = new ProductDTO(p);
		model.addAttribute("product", dto);
		model.addAttribute("reviews", reviewService.findByProductId(id));

		// Sản phẩm liên quan
		Long categoryId = p.getCategory() != null ? p.getCategory().getId().longValue() : null;
		List<Product> related = (categoryId != null) ? productService.findRelatedProducts(categoryId, p.getId())
				: List.of();

		model.addAttribute("relatedProducts", related);

		return "quickview";
	}

	@PostMapping("/laptops/review")
	public String submitReview(@RequestParam("productId") Long productId,
			@RequestParam("customerName") String customerName, @RequestParam("email") String email,
			@RequestParam("content") String content, @RequestParam("starValue") Integer starValue) {

		Product product = productService.findById(productId).orElse(null);
		if (product == null) {
			return "redirect:/error";
		}

		// Kiểm tra customer đã tồn tại chưa
		Customer customer = customerService.findByEmail(email);
		if (customer == null) {
			customer = new Customer();
			customer.setName(customerName);
			customer.setEmail(email);
			customerService.save(customer);
		}

		Review review = new Review();
		review.setProduct(product);
		review.setCustomer(customer);
		review.setContent(content);
		review.setStarValue(starValue);

		reviewService.save(review);

		return "redirect:/product/quickview/" + productId;
	}

}

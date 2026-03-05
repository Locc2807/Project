package com.bkap.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bkap.entity.Product;
import com.bkap.services.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

	@Autowired
	private ProductService productService;

	// API: Thêm sản phẩm vào giỏ
	@PostMapping("/cart/add/{id}")
	@ResponseBody
	public ResponseEntity<?> addToCart(
			@PathVariable Long id,
			@RequestParam(defaultValue = "1") int quantity,
			HttpSession session) {
		
		System.out.println("=== ADD TO CART DEBUG ===");
		System.out.println("Product ID: " + id);
		System.out.println("Quantity: " + quantity);
		System.out.println("Session ID: " + session.getId());
		
		Cart cart = getCart(session);
		System.out.println("Cart before add - Total items: " + cart.getTotalItems());

		Optional<Product> optionalProduct = productService.findById(id);
		if (optionalProduct.isPresent()) {
			Product product = optionalProduct.get();
			System.out.println("Product found: " + product.getName());
			cart.addItem(product, quantity);
			session.setAttribute("cart", cart);
			
			System.out.println("Cart after add - Total items: " + cart.getTotalItems());
			System.out.println("========================");
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Đã thêm vào giỏ hàng");
			response.put("totalItems", cart.getTotalItems());
			response.put("totalPrice", cart.getTotalPrice());
			
			return ResponseEntity.ok(response);
		}

		System.out.println("Product NOT found!");
		System.out.println("========================");
		return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy sản phẩm"));
	}

	// API: Cập nhật số lượng
	@PostMapping("/cart/update/{id}")
	@ResponseBody
	public ResponseEntity<?> updateCart(
			@PathVariable Long id,
			@RequestParam int quantity,
			HttpSession session) {
		
		Cart cart = getCart(session);
		cart.updateQuantity(id, quantity);
		session.setAttribute("cart", cart);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("totalItems", cart.getTotalItems());
		response.put("totalPrice", cart.getTotalPrice());

		return ResponseEntity.ok(response);
	}

	// API: Xóa sản phẩm khỏi giỏ
	@PostMapping("/cart/remove/{id}")
	@ResponseBody
	public ResponseEntity<?> removeFromCart(@PathVariable Long id, HttpSession session) {
		Cart cart = getCart(session);
		cart.removeItem(id);
		session.setAttribute("cart", cart);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "Đã xóa khỏi giỏ hàng");
		response.put("totalItems", cart.getTotalItems());
		response.put("totalPrice", cart.getTotalPrice());

		return ResponseEntity.ok(response);
	}

	// API: Xóa toàn bộ giỏ hàng
	@PostMapping("/cart/clear")
	@ResponseBody
	public ResponseEntity<?> clearCart(HttpSession session) {
		Cart cart = getCart(session);
		cart.clear();
		session.setAttribute("cart", cart);

		return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa toàn bộ giỏ hàng"));
	}

	// Trang giỏ hàng
	@GetMapping("/cart")
	public String viewCart(HttpSession session, Model model) {
		System.out.println("=== VIEW CART DEBUG ===");
		System.out.println("Session ID: " + session.getId());
		
		Cart cart = getCart(session);
		System.out.println("Cart total items: " + cart.getTotalItems());
		System.out.println("Cart items: " + cart.getCartItems().size());
		System.out.println("======================");
		
		model.addAttribute("cart", cart);
		model.addAttribute("cartItems", cart.getCartItems());
		model.addAttribute("totalPrice", cart.getTotalPrice());
		model.addAttribute("totalItems", cart.getTotalItems());
		
		return "cart";
	}

	// Helper method
	private Cart getCart(HttpSession session) {
		Cart cart = (Cart) session.getAttribute("cart");
		if (cart == null) {
			cart = new Cart();
			session.setAttribute("cart", cart);
		}
		return cart;
	}
}

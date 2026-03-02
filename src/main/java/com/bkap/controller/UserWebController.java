package com.bkap.controller;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bkap.dto.OrderViewModel;
import com.bkap.entity.Category;
import com.bkap.entity.Customer;
import com.bkap.entity.Orders;
import com.bkap.entity.Product;
import com.bkap.entity.User;
import com.bkap.services.CategoryService;
import com.bkap.services.OrderService;
import com.bkap.services.ProductService;
import com.bkap.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserWebController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	// === Trang chủ ===
	@GetMapping("/")
	public String homePage(Model model, @RequestParam(value = "loginSuccess", required = false) String loginSuccess) {
		if ("true".equals(loginSuccess)) {
			model.addAttribute("message", "Đăng nhập thành công!");
		}

		model.addAttribute("newProducts", productService.findTop3Latest());
		model.addAttribute("topProducts", productService.findTop3Latest());

		return "index";
	}

	// === Trang đăng nhập ===
	@GetMapping("/user/login")
	public String userLogin(@RequestParam(value = "registerSuccess", required = false) String registerSuccess,
			@RequestParam(value = "error", required = false) String error, HttpServletRequest request, Model model) {

		if ("true".equals(registerSuccess)) {
			model.addAttribute("registerSuccess", true);
		}
		if (error != null) {
			model.addAttribute("loginError", true);
			String errorMessage = (String) request.getSession().getAttribute("errorMessage");
			model.addAttribute("errorMessage", errorMessage);
			request.getSession().removeAttribute("errorMessage");
		}

		return "/user/login";
	}

	// === Đăng ký ===
	@GetMapping("/register")
	public String userRegister() {
		return "/user/register";
	}

	@PostMapping("/user/register")
	public String handleRegister(@RequestParam("username") String username, @RequestParam("email") String email,
			@RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword,
			RedirectAttributes redirect) {

		if (!password.equals(confirmPassword)) {
			redirect.addAttribute("error", "Mật khẩu không khớp");
			return "redirect:/user/register";
		}

		if (userService.findByUserName(username) != null) {
			redirect.addAttribute("error", "Tên đăng nhập đã tồn tại");
			return "redirect:/user/register";
		}

		if (userService.findByEmail(email) != null) {
			redirect.addAttribute("error", "Email đã tồn tại");
			return "redirect:/user/register";
		}

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		user.setRole("ROLE_USER");
		user.setEnabled(true);

		// Tạo Customer và ánh xạ 1-1
		Customer customer = new Customer();
		customer.setName(username);
		customer.setEmail(email);
		customer.setCreated(new Date());
		customer.setUser(user);

		user.setCustomer(customer); // Liên kết 2 chiều

		userService.save(user); // save sẽ cascade customer do @OneToOne(cascade = ALL)
		return "redirect:/user/login?registerSuccess=true";
	}

	// === Quên mật khẩu ===
	@GetMapping("/user/forgot_password")
	public String forgotPasswordPage() {
		return "user/forgot_password";
	}

	@PostMapping("/user/forgot_password")
	public String handleForgotPassword(@RequestParam("email") String email, RedirectAttributes redirect) {
		User user = userService.findByEmail(email);
		if (user != null) {
			redirect.addFlashAttribute("message", "Đã gửi email đặt lại mật khẩu (giả lập).");
		} else {
			redirect.addFlashAttribute("message", "Email không tồn tại.");
		}
		return "redirect:/user/forgot_password";
	}

	// === Hồ sơ người dùng ===
	@GetMapping("/user/profile")
	public String showProfilePage(Model model, Principal principal) {
		User user = userService.findByUserName(principal.getName());
		System.out.println(">> principal.getName(): " + principal.getName());
		if (user == null || user.getCustomer() == null) {
			return "redirect:/";
		}

		model.addAttribute("user", user);
		model.addAttribute("currentPage", "profile");
		return "user/profile";
	}

	// === Danh sách danh mục ===
	@GetMapping("/categories")
	public String getCategoriesPage(@RequestParam(value = "id", required = false) Integer id, Model model) {
		if (id != null) {
			Category selected = categoryService.findById(id);
			model.addAttribute("categories", selected != null ? List.of(selected) : List.of());
			model.addAttribute("selectedId", id);
		} else {
			model.addAttribute("categories", categoryService.getActiveCategories());
			model.addAttribute("selectedId", null);
		}
		return "categories";
	}

	// === Chi tiết danh mục ===
	@GetMapping("/categories/{id}")
	public String getCategoryDetail(@PathVariable("id") int id, Model model,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "show", defaultValue = "6") int pageSize,
			@RequestParam(name = "sort", defaultValue = "name") String sortField) {

		Category category = categoryService.findById(id);
		if (category == null) {
			return "redirect:/categories";
		}

		int pageIndex = Math.max(0, page - 1);
		Sort sort = switch (sortField.toLowerCase()) {
		case "price" -> Sort.by("price").ascending();
		case "name" -> Sort.by("name").ascending();
		default -> Sort.by("name").ascending();
		};

		Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);
		Page<Product> productPage = productService.findByCategoryIdWithPageable(id, pageable);

		List<Product> topSelling = productService.findTop3LatestByCategory(category.getName().toLowerCase());

		model.addAttribute("category", category);
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("currentPage", page);
		model.addAttribute("topSelling", topSelling);
		model.addAttribute("selectedSort", sortField);
		model.addAttribute("selectedShow", pageSize);

		return switch (category.getName().toLowerCase()) {
		case "laptops" -> "laptops";
		case "smartphones" -> "smartphones";
		case "cameras" -> "cameras";
		case "accessories" -> "accessories";
		default -> "categories";
		};
	}

	// === Laptops page ===
	@GetMapping("/laptops")
	public String showLaptop(@RequestParam(name = "brands", required = false) List<String> brands,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "sort", defaultValue = "name") String sortField,
			@RequestParam(name = "show", defaultValue = "6") int pageSize, Model model) {

		if (page < 1)
			page = 1;
		int pageIndex = page - 1;

		Sort sort = switch (sortField.toLowerCase()) {
		case "price" -> Sort.by("price").ascending();
		case "name" -> Sort.by("name").ascending();
		default -> Sort.by("name").ascending();
		};

		Page<Product> laptopPage;
		Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

		if (brands != null && !brands.isEmpty()) {
			laptopPage = productService.findLaptopsByBrandsWithPageable(brands, pageable);
			model.addAttribute("brands", brands);
		} else {
			laptopPage = productService.findLaptopsWithPageable(pageable);
		}

		// Vượt trang
		if (pageIndex >= laptopPage.getTotalPages() && laptopPage.getTotalPages() > 0) {
			return "redirect:/laptops?page=" + laptopPage.getTotalPages();
		}

		List<Product> topSelling = productService.findTop3Latest();

		model.addAttribute("products", laptopPage.getContent());
		model.addAttribute("totalPages", laptopPage.getTotalPages());
		model.addAttribute("currentPage", page);
		model.addAttribute("topSelling", topSelling);
		model.addAttribute("selectedSort", sortField);
		model.addAttribute("selectedShow", pageSize);

		return "laptops";
	}

	@PostMapping("/laptops/filter")
	public String filterLaptops(@RequestBody List<String> brands, Model model) {
		List<Product> filtered = (brands == null || brands.isEmpty())
				? productService.getAll().stream().filter(p -> "laptops".equalsIgnoreCase(p.getCategory().getName()))
						.toList()
				: productService.getAll().stream().filter(
						p -> "laptops".equalsIgnoreCase(p.getCategory().getName()) && brands.contains(p.getBrand()))
						.toList();

		model.addAttribute("products", filtered);
		return "laptops :: #product-list";
	}

	// === Smartphones page ===
	@GetMapping("/smartphones")
	public String showSmartPhone(@RequestParam(name = "brands", required = false) List<String> brands,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "sort", defaultValue = "name") String sortField,
			@RequestParam(name = "show", defaultValue = "6") int pageSize, Model model) {

		if (page < 1)
			page = 1;
		int pageIndex = page - 1;

		Sort sort = switch (sortField.toLowerCase()) {
		case "price" -> Sort.by("price").ascending();
		case "name" -> Sort.by("name").ascending();
		default -> Sort.by("name").ascending();
		};

		Page<Product> smartphonePage;
		Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

		if (brands != null && !brands.isEmpty()) {
			smartphonePage = productService.findSmartphonesByBrandsWithPageable(brands, pageable);
			model.addAttribute("brands", brands);
		} else {
			smartphonePage = productService.findSmartphonesWithPageable(pageable);
		}

		// Vượt trang
		if (pageIndex >= smartphonePage.getTotalPages() && smartphonePage.getTotalPages() > 0) {
			return "redirect:/smartphones?page=" + smartphonePage.getTotalPages();
		}

		List<Product> topSelling = productService.findTop3LatestByCategory("smartphones");

		model.addAttribute("products", smartphonePage.getContent());
		model.addAttribute("totalPages", smartphonePage.getTotalPages());
		model.addAttribute("currentPage", page);
		model.addAttribute("topSelling", topSelling);
		model.addAttribute("selectedSort", sortField);
		model.addAttribute("selectedShow", pageSize);

		return "smartphones";
	}

	@PostMapping("/smartphones/filter")
	public String filterSmartPhone(@RequestBody List<String> brands, Model model) {
		List<Product> filtered;

		if (brands == null || brands.isEmpty()) {
			filtered = productService.getByCategoryName("smartphones");
		} else {
			filtered = productService.findByCategoryAndBrands("smartphones", brands);
		}

		model.addAttribute("products", filtered);
		return "smartphones :: #product-list";
	}

	// === Cameras page ===
	@GetMapping("/cameras")
	public String showCamera(@RequestParam(name = "brands", required = false) List<String> brands,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "sort", defaultValue = "name") String sortField,
			@RequestParam(name = "show", defaultValue = "6") int pageSize, Model model) {

		if (page < 1)
			page = 1;
		int pageIndex = page - 1;

		Sort sort = switch (sortField.toLowerCase()) {
		case "price" -> Sort.by("price").ascending();
		case "name" -> Sort.by("name").ascending();
		default -> Sort.by("name").ascending();
		};

		Page<Product> cameraPage;
		Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

		if (brands != null && !brands.isEmpty()) {
			cameraPage = productService.findCamerasByBrandsWithPageable(brands, pageable);
			model.addAttribute("brands", brands);
		} else {
			cameraPage = productService.findCamerasWithPageable(pageable);
		}

		// Vượt trang
		if (pageIndex >= cameraPage.getTotalPages() && cameraPage.getTotalPages() > 0) {
			return "redirect:/cameras?page=" + cameraPage.getTotalPages();
		}

		List<Product> topSelling = productService.findTop3LatestCamerasByCategory("cameras");

		model.addAttribute("products", cameraPage.getContent());
		model.addAttribute("totalPages", cameraPage.getTotalPages());
		model.addAttribute("currentPage", page);
		model.addAttribute("topSelling", topSelling);
		model.addAttribute("selectedSort", sortField);
		model.addAttribute("selectedShow", pageSize);

		return "cameras";
	}

	@PostMapping("/cameras/filter")
	public String filterCamera(@RequestBody List<String> brands, Model model) {
		List<Product> filtered;

		if (brands == null || brands.isEmpty()) {
			filtered = productService.getByCategoryName("cameras");
		} else {
			filtered = productService.findByCategoryAndBrands("cameras", brands);
		}

		model.addAttribute("products", filtered);
		return "smartphones :: #product-list";
	}

	// === Accessories page ===
	@GetMapping("/accessories")
	public String showAccessories(@RequestParam(name = "brands", required = false) List<String> brands,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "sort", defaultValue = "name") String sortField,
			@RequestParam(name = "show", defaultValue = "6") int pageSize, Model model) {

		if (page < 1)
			page = 1;
		int pageIndex = page - 1;

		Sort sort = switch (sortField.toLowerCase()) {
		case "price" -> Sort.by("price").ascending();
		case "name" -> Sort.by("name").ascending();
		default -> Sort.by("name").ascending();
		};

		Page<Product> accessoryPage;
		Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

		if (brands != null && !brands.isEmpty()) {
			accessoryPage = productService.findAccessoriesByBrandsWithPageable(brands, pageable);
			model.addAttribute("brands", brands);
		} else {
			accessoryPage = productService.findAccessoriesWithPageable(pageable);
		}

		// Vượt trang
		if (pageIndex >= accessoryPage.getTotalPages() && accessoryPage.getTotalPages() > 0) {
			return "redirect:/accessories?page=" + accessoryPage.getTotalPages();
		}

		List<Product> topSelling = productService.findTop3LatestAccessoriesByCategory("accessories");

		model.addAttribute("products", accessoryPage.getContent());
		model.addAttribute("totalPages", accessoryPage.getTotalPages());
		model.addAttribute("currentPage", page);
		model.addAttribute("topSelling", topSelling);
		model.addAttribute("selectedSort", sortField);
		model.addAttribute("selectedShow", pageSize);

		return "accessories";
	}

	@PostMapping("/accessories/filter")
	public String filterAccessory(@RequestBody List<String> brands, Model model) {
		List<Product> filtered;

		if (brands == null || brands.isEmpty()) {
			filtered = productService.getByCategoryName("accessories");
		} else {
			filtered = productService.findByCategoryAndBrands("accessories", brands);
		}

		model.addAttribute("products", filtered);
		return "accessories :: #product-list";
	}

	@GetMapping("/user/orders")
	public String viewUserOrders(Model model, @RequestParam(value = "status", required = false) Integer status,
			Principal principal) {
		User user = userService.findByUserName(principal.getName());
		if (user == null || user.getCustomer() == null) {
			return "redirect:/";
		}
		List<Orders> orders = (status == null) ? orderService.getOrderByUser(user.getCustomer())
				: orderService.getOrdersByUserAndStatus(user.getCustomer(), status);

		List<OrderViewModel> viewModels = orders.stream().map(OrderViewModel::new).toList();
		model.addAttribute("orders", viewModels);
		model.addAttribute("currentStatus", status);
		model.addAttribute("currentPage", "orders");
		return "user/orders"; // trả về template Thymeleaf: user/orders.html
	}

	// Đơn khách đánh dấu đã nhận
	@GetMapping("/orders/received/{id}")
	public String markOrderAsReceived(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
		Orders order = orderService.findById(id);
		if (order != null && order.getCustomer().getEmail().equals(principal.getName()) && order.getStatus() == 4) {
			orderService.updateStatus(id, 6);
			ra.addFlashAttribute("message", "Cập nhật đơn hàng thành công!");
		}
		return "redirect:/user/orders";
	}

	// Đơn khách hủy khi chưa xác nhận
	@GetMapping("/orders/cancel/{id}")
	public String cancelOrder(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
		Orders order = orderService.findById(id);
		if (order != null && order.getCustomer().getEmail().equals(principal.getName()) && order.getStatus() == 1) {
			orderService.updateStatus(id, 5);
			ra.addFlashAttribute("message", "Đã hủy đơn hàng.");
		}
		return "redirect:/user/orders";
	}

}

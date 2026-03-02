package com.bkap.controller.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bkap.dto.OrderViewModel;
import com.bkap.dto.ReviewViewModel;
import com.bkap.entity.Customer;
import com.bkap.entity.HotDeal;
import com.bkap.entity.Orders;
import com.bkap.entity.Product;
import com.bkap.entity.Review;
import com.bkap.entity.User;
import com.bkap.repository.ReviewRepository;
import com.bkap.services.CustomerService;
import com.bkap.services.HotDealService;
import com.bkap.services.OrderService;
import com.bkap.services.ProductService;
import com.bkap.services.ReviewService;
import com.bkap.services.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private HotDealService hotDealService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ProductService productService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("productCount", productService.countAll());
		model.addAttribute("orderToday", orderService.countTodayOrders());
		model.addAttribute("userCount", userService.countAllUsers());
		model.addAttribute("totalRevenue", orderService.getTodayRevenue());

		return "admin/dashboard";
	}

	@GetMapping("/order")
	public String showOrders(@RequestParam(value = "status", required = false) Integer status,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Model model) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());
		Page<Orders> ordersPage = orderService.findByFilter(status, keyword, pageable);

		List<OrderViewModel> orderVMs = ordersPage.getContent().stream().map(OrderViewModel::new)
				.collect(Collectors.toList());

		model.addAttribute("orders", orderVMs);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", ordersPage.getTotalPages());
		model.addAttribute("status", status);
		model.addAttribute("keyword", keyword);

		return "admin/order"; // View template đã đúng
	}

	@GetMapping("/customer")
	public String viewCustomers(Model model, @RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page) {

		int pageSize = 10;
		Page<Customer> customersPage;

		if (keyword != null && !keyword.trim().isEmpty()) {
			customersPage = customerService.searchByKeyword(keyword, page, pageSize);
		} else {
			customersPage = customerService.findAll(page, pageSize);
		}

		model.addAttribute("customersPage", customersPage);
		model.addAttribute("customers", customersPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", customersPage.getTotalPages());
		model.addAttribute("keyword", keyword);

		return "admin/customer";
	}

	@GetMapping("")
	public String adminRootRedirect() {
		return "redirect:/admin/dashboard";
	}

	@GetMapping("/customers/orders/{id}")
	public String viewCustomerOrders(@PathVariable Long id, Model model) {
		List<Orders> orders = orderService.findByCustomerId(id);
		List<OrderViewModel> orderVMs = orders.stream().map(OrderViewModel::new).collect(Collectors.toList());

		model.addAttribute("customer", customerService.findById(id).orElse(null));
		model.addAttribute("orders", orderVMs);

		return "admin/customers/customer-orders";
	}

	@GetMapping("/reviews")
	public String viewAllReviews(Model model, @RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		PageRequest pageable = PageRequest.of(page, size, Sort.by("created").descending());
		Page<Review> reviewPage = reviewService.findAll(keyword, pageable);

		List<ReviewViewModel> reviewVMs = reviewPage.getContent().stream().map(ReviewViewModel::new).toList();

		model.addAttribute("reviews", reviewVMs);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", reviewPage.getTotalPages());
		model.addAttribute("keyword", keyword);

		return "admin/review/index";
	}

	@GetMapping("/customers/lock/{id}")
	public String showLockForm(@PathVariable Long id, Model model) {
		Customer customer = customerService.findById(id).orElse(null);
		model.addAttribute("customer", customer);
		return "admin/customers/lock-form";
	}

	@PostMapping("/customers/lock")
	public String lockCustomer(@RequestParam Long customerId, @RequestParam String reason) {
		Customer customer = customerService.findById(customerId).orElse(null);
		if (customer != null && customer.getUser() != null) {
			User user = customer.getUser();
			user.setEnabled(false); // Khoá tài khoản
			userService.save(user);

			// TODO: Ghi log hoặc lưu lý do nếu có bảng lưu lịch sử khoá
			// Ví dụ: lockLogService.save(new LockLog(user.getId(), reason));
		}
		return "redirect:/admin/customer";
	}

	@GetMapping("/customers/unlock/{id}")
	public String showUnlockForm(@PathVariable Long id, Model model) {
		Customer customer = customerService.findById(id).orElse(null);
		model.addAttribute("customer", customer);
		return "admin/customers/unlock-form";
	}

	@PostMapping("/customers/unlock")
	public String unlockCustomer(@RequestParam Long customerId) {
		Customer customer = customerService.findById(customerId).orElse(null);
		if (customer != null && customer.getUser() != null) {
			User user = customer.getUser();
			user.setEnabled(true);
			userService.save(user);
		}
		return "redirect:/admin/customer";
	}

	@GetMapping("/hotdeal")
	public String index(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "status", required = false) Integer status,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "4") int size, Model model) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
		Page<HotDeal> deals = hotDealService.filter(keyword, status, startDate, endDate, pageable);

		model.addAttribute("hotDeals", deals.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", deals.getTotalPages());

		model.addAttribute("keyword", keyword);
		model.addAttribute("status", status);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);

		return "admin/hotdeal/index";
	}

	@GetMapping("/add-hotdeal")
	public String add(Model model) {
		model.addAttribute("hotDeal", new HotDeal());
		model.addAttribute("products", productService.findAll());
		return "admin/hotdeal/add";
	}

	@PostMapping("/add-hotdeal")
	public String save(@ModelAttribute HotDeal hotDeal, @RequestParam("productId") Long productId,
			RedirectAttributes ra, Model model) {

		Product product = productService.findById(productId).orElse(null); // hoặc .orElseThrow(...) nếu bạn muốn

		if (product == null) {
			model.addAttribute("error", "Sản phẩm không tồn tại");
			model.addAttribute("products", productService.findAll());
			return "admin/hotdeal/add";
		}

		// Validate ngày
		if (hotDeal.getStartDate() != null && hotDeal.getEndDate() != null
				&& hotDeal.getStartDate().isAfter(hotDeal.getEndDate())) {
			model.addAttribute("error", "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
			model.addAttribute("products", productService.findAll());
			return "admin/hotdeal/add";
		}

		// Validate phần trăm giảm
		if (hotDeal.getDiscountPercent() < 0 || hotDeal.getDiscountPercent() > 100) {
			model.addAttribute("error", "Phần trăm giảm phải từ 0 đến 100");
			model.addAttribute("products", productService.findAll());
			return "admin/hotdeal/add";
		}

		// Gán sản phẩm và lưu
		hotDeal.setProduct(product);
		hotDealService.save(hotDeal);

		ra.addFlashAttribute("success", "Thêm Hot Deal thành công");
		return "redirect:/admin/hotdeal";
	}

	@GetMapping("/edit-hotdeal/{id}")
	public String edit(@PathVariable("id") Long id, Model model) {
		HotDeal hotDeal = hotDealService.findById(id).orElse(null);
		List<Product> products = productService.findAll();

		model.addAttribute("hotDeal", hotDeal);
		model.addAttribute("products", products);
		return "admin/hotdeal/edit";
	}

	@PostMapping("/edit-hotdeal/{id}")
	public String update(@PathVariable("id") Long id, @ModelAttribute("hotDeal") HotDeal hotDeal,
			RedirectAttributes redirect) {
		hotDeal.setId(id);
		if (hotDealService.update(hotDeal)) {
			redirect.addFlashAttribute("success", "Cập nhật Hot Deal thành công");
			return "redirect:/admin/hotdeal";
		} else {
			redirect.addFlashAttribute("error", "Cập nhật thất bại");
			return "redirect:/admin/edit-hotdeal/" + id;
		}
	}

	@GetMapping("/delete-hotdeal/{id}")
	public String delete(@PathVariable("id") Long id, RedirectAttributes redirect) {
		if (hotDealService.delete(id)) {
			redirect.addFlashAttribute("success", "Xóa Hot Deal thành công");
		} else {
			redirect.addFlashAttribute("error", "Xóa Hot Deal thất bại");
		}
		return "redirect:/admin/hotdeal";
	}

}

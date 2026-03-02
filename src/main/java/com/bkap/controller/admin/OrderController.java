package com.bkap.controller.admin;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bkap.dto.OrderStatisticsDTO;
import com.bkap.dto.OrderViewModel;
import com.bkap.dto.ProductSalesDTO;
import com.bkap.entity.OrderNote;
import com.bkap.entity.OrderStatusHistory;
import com.bkap.entity.Orders;
import com.bkap.exception.InvalidOrderStatusTransitionException;
import com.bkap.services.OrderService;
import com.bkap.services.OrderStatusValidator;
import com.bkap.services.PdfInvoiceService;

@Controller
@RequestMapping("/admin/order")
public class OrderController {

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderStatusValidator statusValidator;
	
	@Autowired
	private PdfInvoiceService pdfInvoiceService;

	// Xem chi tiết đơn hàng
	@GetMapping("/{id}")
	public String viewOrder(@PathVariable Long id, Model model) {
		Orders order = orderService.findById(id);
		if (order == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
		}

		OrderViewModel wrapper = new OrderViewModel(order);
		
		// Lấy danh sách trạng thái có thể chuyển đến
		List<Integer> validNextStatuses = statusValidator.getValidNextStatuses(order.getStatus());
		
		// Lấy lịch sử thay đổi trạng thái
		List<OrderStatusHistory> statusHistory = orderService.getStatusHistory(id);
		
		// Lấy danh sách ghi chú
		List<OrderNote> notes = orderService.getNotes(id);

		model.addAttribute("order", order);
		model.addAttribute("wrapper", wrapper);
		model.addAttribute("validNextStatuses", validNextStatuses);
		model.addAttribute("currentStatusName", statusValidator.getStatusName(order.getStatus()));
		model.addAttribute("statusHistory", statusHistory);
		model.addAttribute("notes", notes);
		model.addAttribute("statusValidator", statusValidator);
		
		return "admin/order/detail";
	}

	// Duyệt đơn hàng (từ trạng thái 1 → 2)
	@PostMapping("/confirm/{id}")
	public String confirm(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
		try {
			String username = principal != null ? principal.getName() : "ADMIN";
			orderService.updateStatus(id, OrderStatusValidator.STATUS_CONFIRMED, username);
			ra.addFlashAttribute("message", "Đơn hàng đã được xác nhận.");
		} catch (InvalidOrderStatusTransitionException e) {
			ra.addFlashAttribute("error", e.getMessage());
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/order";
	}

	// Hủy đơn hàng (từ trạng thái 1 hoặc 7 → 5)
	@PostMapping("/cancel/{id}")
	public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
		try {
			String username = principal != null ? principal.getName() : "ADMIN";
			orderService.updateStatus(id, OrderStatusValidator.STATUS_CANCELLED, username);
			ra.addFlashAttribute("message", "Đơn hàng đã bị hủy.");
		} catch (InvalidOrderStatusTransitionException e) {
			ra.addFlashAttribute("error", e.getMessage());
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/order";
	}

	// Cập nhật trạng thái tùy ý
	@PostMapping("/updateStatus/{id}/{status}")
	public String updateStatus(@PathVariable Long id, @PathVariable int status, 
	                           Principal principal, RedirectAttributes ra) {
		if (status < 1 || status > 7) {
			ra.addFlashAttribute("error", "Trạng thái không hợp lệ.");
			return "redirect:/admin/order";
		}

		try {
			String username = principal != null ? principal.getName() : "ADMIN";
			orderService.updateStatus(id, status, username);
			ra.addFlashAttribute("message", "Cập nhật trạng thái thành công.");
		} catch (InvalidOrderStatusTransitionException e) {
			ra.addFlashAttribute("error", e.getMessage());
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/order";
	}
	
	// Bulk confirm orders
	@PostMapping("/bulk-confirm")
	public String bulkConfirm(@RequestParam("orderIds") List<Long> orderIds, 
	                          Principal principal, RedirectAttributes ra) {
		try {
			String username = principal != null ? principal.getName() : "ADMIN";
			orderService.bulkUpdateStatus(orderIds, OrderStatusValidator.STATUS_CONFIRMED, username);
			ra.addFlashAttribute("message", "Đã xác nhận " + orderIds.size() + " đơn hàng.");
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/order";
	}
	
	// Bulk cancel orders
	@PostMapping("/bulk-cancel")
	public String bulkCancel(@RequestParam("orderIds") List<Long> orderIds, 
	                         Principal principal, RedirectAttributes ra) {
		try {
			String username = principal != null ? principal.getName() : "ADMIN";
			orderService.bulkUpdateStatus(orderIds, OrderStatusValidator.STATUS_CANCELLED, username);
			ra.addFlashAttribute("message", "Đã hủy " + orderIds.size() + " đơn hàng.");
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/order";
	}
	
	// API: Lấy top sản phẩm bán chạy
	@GetMapping("/api/top-products")
	@ResponseBody
	public ResponseEntity<List<ProductSalesDTO>> getTopProducts(
			@RequestParam(defaultValue = "10") int limit) {
		List<ProductSalesDTO> topProducts = orderService.getTopSellingProducts(limit);
		return ResponseEntity.ok(topProducts);
	}
	
	// API: Thống kê đơn hàng theo khoảng thời gian
	@GetMapping("/api/statistics")
	@ResponseBody
	public ResponseEntity<List<OrderStatisticsDTO>> getStatistics(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		List<OrderStatisticsDTO> statistics = orderService.getOrderStatisticsByDateRange(startDate, endDate);
		return ResponseEntity.ok(statistics);
	}
	
	// Trang báo cáo thống kê
	@GetMapping("/reports")
	public String showReports(Model model) {
		// Thống kê 30 ngày gần nhất
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(30);
		
		List<OrderStatisticsDTO> statistics = orderService.getOrderStatisticsByDateRange(startDate, endDate);
		List<ProductSalesDTO> topProducts = orderService.getTopSellingProducts(10);
		double cancellationRate = orderService.getCancellationRate();
		
		model.addAttribute("statistics", statistics);
		model.addAttribute("topProducts", topProducts);
		model.addAttribute("cancellationRate", cancellationRate);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		
		return "admin/order/reports";
	}

	
	// Download hóa đơn PDF
	@GetMapping("/{id}/invoice")
	public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
		Orders order = orderService.findById(id);
		if (order == null) {
			return ResponseEntity.notFound().build();
		}
		
		byte[] pdfContent = pdfInvoiceService.generateInvoicePDF(order);
		
		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=invoice-" + id + ".txt")
				.header("Content-Type", "text/plain; charset=UTF-8")
				.body(pdfContent);
	}
	
	// Download phiếu giao hàng
	@GetMapping("/{id}/delivery-note")
	public ResponseEntity<byte[]> downloadDeliveryNote(@PathVariable Long id) {
		Orders order = orderService.findById(id);
		if (order == null) {
			return ResponseEntity.notFound().build();
		}
		
		byte[] pdfContent = pdfInvoiceService.generateDeliveryNotePDF(order);
		
		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=delivery-note-" + id + ".txt")
				.header("Content-Type", "text/plain; charset=UTF-8")
				.body(pdfContent);
	}

	
	// API: Lấy lịch sử thay đổi trạng thái
	@GetMapping("/{id}/history")
	@ResponseBody
	public ResponseEntity<List<OrderStatusHistory>> getStatusHistory(@PathVariable Long id) {
		List<OrderStatusHistory> history = orderService.getStatusHistory(id);
		return ResponseEntity.ok(history);
	}
	
	// API: Thêm ghi chú
	@PostMapping("/{id}/add-note")
	public String addNote(@PathVariable Long id,
	                      @RequestParam String content,
	                      @RequestParam(defaultValue = "true") Boolean isInternal,
	                      Principal principal,
	                      RedirectAttributes ra) {
		try {
			String username = principal != null ? principal.getName() : "ADMIN";
			orderService.addNote(id, content, username, isInternal);
			ra.addFlashAttribute("message", "Đã thêm ghi chú thành công.");
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/order/" + id;
	}
	
	// API: Lấy danh sách ghi chú
	@GetMapping("/{id}/notes")
	@ResponseBody
	public ResponseEntity<List<OrderNote>> getNotes(@PathVariable Long id) {
		List<OrderNote> notes = orderService.getNotes(id);
		return ResponseEntity.ok(notes);
	}
}

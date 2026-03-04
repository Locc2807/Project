package com.bkap.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.dto.OrderStatisticsDTO;
import com.bkap.dto.ProductSalesDTO;
import com.bkap.entity.Customer;
import com.bkap.entity.OrderDetail;
import com.bkap.entity.OrderNote;
import com.bkap.entity.OrderStatusHistory;
import com.bkap.entity.Orders;
import com.bkap.entity.Review;
import com.bkap.exception.InvalidOrderStatusTransitionException;
import com.bkap.repository.OrderDetailRepository;
import com.bkap.repository.OrderNoteRepository;
import com.bkap.repository.OrderRepository;
import com.bkap.repository.OrderStatusHistoryRepository;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderDetailRepository orderDetailRepository;
	
	@Autowired
	private OrderStatusValidator statusValidator;
	
	@Autowired
	private InventoryService inventoryService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private OrderStatusHistoryRepository statusHistoryRepository;
	
	@Autowired
	private OrderNoteRepository noteRepository;

	@Override
	public List<Orders> findAll() {
		return orderRepository.findAll();
	}

	@Override
	public Orders findById(Long id) {
		return orderRepository.findById(id).orElse(null);
	}

	@Override
	public Orders save(Orders order) {
		return orderRepository.save(order);
	}

	@Override
	public Boolean create(Orders order) {
		try {
			orderRepository.save(order);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Boolean createOrderDetail(com.bkap.entity.OrderDetail orderDetail) {
		try {
			orderDetailRepository.save(orderDetail);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void deleteById(Long id) {
		orderRepository.deleteById(id);
	}

	@Override
	public List<Orders> searchOrders(String keyword, Integer status) {
		return orderRepository.searchOrders(keyword, status);
	}

	@Override
	public List<Orders> getOrderByUser(Customer customer) {
		// TODO Auto-generated method stub
		return orderRepository.findByCustomer(customer);
	}

	@Override
	public List<Orders> getOrdersByUserAndStatus(Customer customer, Integer status) {
		// TODO Auto-generated method stub
		return orderRepository.findByCustomerAndStatus(customer, status);
	}

	@Override
	public long countTodayOrders() {
		// TODO Auto-generated method stub
		return orderRepository.countTodayOrders();
	}

	@Override
	public double getTodayRevenue() {
		Double revenue = orderRepository.getTodayRevenue();
		return revenue != null ? revenue : 0;
	}

	@Override
	@Transactional
	public void updateStatus(Long id, int status) {
		updateStatus(id, status, "SYSTEM");
	}
	
	@Override
	@Transactional
	public void updateStatus(Long id, int status, String changedBy) {
		Orders order = orderRepository.findById(id).orElse(null);
		if (order == null) {
			throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + id);
		}
		
		int oldStatus = order.getStatus();
		
		// Validate chuyển trạng thái
		if (!statusValidator.isValidTransition(oldStatus, status)) {
			throw new InvalidOrderStatusTransitionException(
				statusValidator.getTransitionErrorMessage(oldStatus, status),
				oldStatus,
				status
			);
		}
		
		// Xử lý inventory khi xác nhận đơn (1 → 2)
		if (oldStatus == OrderStatusValidator.STATUS_PENDING && 
		    status == OrderStatusValidator.STATUS_CONFIRMED) {
			handleOrderConfirmation(order, changedBy);
			order.setConfirmedAt(LocalDateTime.now()); // Cập nhật thời gian xác nhận
		}
		
		// Xử lý inventory khi hủy đơn (→ 5)
		if (status == OrderStatusValidator.STATUS_CANCELLED && oldStatus != OrderStatusValidator.STATUS_CANCELLED) {
			handleOrderCancellation(order, changedBy);
			order.setCancelledAt(LocalDateTime.now()); // Cập nhật thời gian hủy
		}
		
		// Cập nhật các timestamp khác
		if (status == OrderStatusValidator.STATUS_PROCESSING) {
			order.setShippedAt(LocalDateTime.now());
		} else if (status == OrderStatusValidator.STATUS_COMPLETED) {
			order.setDeliveredAt(LocalDateTime.now());
		}
		
		// Cập nhật trạng thái
		order.setStatus(status);
		orderRepository.save(order);
		
		// Lưu lịch sử thay đổi trạng thái
		saveStatusHistory(order, oldStatus, status, changedBy);
		
		// Gửi email thông báo
		sendOrderStatusEmail(order, oldStatus, status);
	}
	
	/**
	 * Lưu lịch sử thay đổi trạng thái
	 */
	private void saveStatusHistory(Orders order, int oldStatus, int newStatus, String changedBy) {
		OrderStatusHistory history = new OrderStatusHistory(order, oldStatus, newStatus, changedBy);
		statusHistoryRepository.save(history);
	}
	
	/**
	 * Xử lý khi xác nhận đơn hàng - trừ tồn kho
	 */
	private void handleOrderConfirmation(Orders order, String changedBy) {
		for (OrderDetail detail : order.getOrderDetails()) {
			Long productId = detail.getProduct().getId();
			Integer quantity = detail.getQuantity();
			
			String note = "Xuất kho cho đơn hàng #" + order.getId();
			boolean success = inventoryService.adjustStock(
				productId, 
				quantity, 
				"EXPORT", 
				note, 
				changedBy
			);
			
			if (!success) {
				throw new RuntimeException(
					"Không đủ tồn kho cho sản phẩm: " + detail.getProduct().getName()
				);
			}
		}
	}
	
	/**
	 * Xử lý khi hủy đơn hàng - hoàn tồn kho
	 */
	private void handleOrderCancellation(Orders order, String changedBy) {
		// Chỉ hoàn kho nếu đơn đã được xác nhận (status >= 2)
		if (order.getStatus() >= OrderStatusValidator.STATUS_CONFIRMED) {
			for (OrderDetail detail : order.getOrderDetails()) {
				Long productId = detail.getProduct().getId();
				Integer quantity = detail.getQuantity();
				
				String note = "Hoàn kho do hủy đơn hàng #" + order.getId();
				inventoryService.adjustStock(
					productId, 
					quantity, 
					"IMPORT", 
					note, 
					changedBy
				);
			}
		}
	}
	
	/**
	 * Gửi email thông báo khi thay đổi trạng thái
	 */
	private void sendOrderStatusEmail(Orders order, int oldStatus, int newStatus) {
		try {
			String customerEmail = order.getCustomer().getEmail();
			if (customerEmail == null || customerEmail.isEmpty()) {
				return;
			}
			
			String subject = "Cập nhật trạng thái đơn hàng #" + order.getId();
			String body = buildEmailBody(order, oldStatus, newStatus);
			
			emailService.sendEmail(customerEmail, subject, body);
		} catch (Exception e) {
			// Log error nhưng không throw exception để không ảnh hưởng đến flow chính
			System.err.println("Lỗi khi gửi email: " + e.getMessage());
		}
	}
	
	/**
	 * Tạo nội dung email
	 */
	private String buildEmailBody(Orders order, int oldStatus, int newStatus) {
		StringBuilder body = new StringBuilder();
		body.append("Xin chào ").append(order.getCustomer().getName()).append(",\n\n");
		body.append("Đơn hàng #").append(order.getId()).append(" của bạn đã được cập nhật.\n\n");
		body.append("Trạng thái cũ: ").append(statusValidator.getStatusName(oldStatus)).append("\n");
		body.append("Trạng thái mới: ").append(statusValidator.getStatusName(newStatus)).append("\n\n");
		
		// Thêm thông tin chi tiết theo trạng thái
		switch (newStatus) {
			case OrderStatusValidator.STATUS_CONFIRMED:
				body.append("Đơn hàng của bạn đã được xác nhận và đang được chuẩn bị.\n");
				break;
			case OrderStatusValidator.STATUS_SHIPPING:
				body.append("Đơn hàng của bạn đang được giao đến địa chỉ.\n");
				break;
			case OrderStatusValidator.STATUS_COMPLETED:
				body.append("Đơn hàng đã được giao thành công. Cảm ơn bạn đã mua hàng!\n");
				break;
			case OrderStatusValidator.STATUS_CANCELLED:
				body.append("Đơn hàng đã bị hủy.\n");
				if (order.getOrder_note() != null) {
					body.append("Lý do: ").append(order.getOrder_note()).append("\n");
				}
				break;
		}
		
		body.append("\nTrân trọng,\n");
		body.append("E-Commerce Team");
		
		return body.toString();
	}

	@Override
	public long countByStatus(int status) {
		// TODO Auto-generated method stub
		return orderRepository.countByStatus(status);
	}

	@Override
	public Page<Orders> findByFilter(Integer status, String keyword, Pageable pageable) {
		if (status != null && keyword != null && !keyword.trim().isEmpty()) {
			return orderRepository.findByStatusAndKeyword(status, keyword.trim(), pageable);
		} else if (status != null) {
			return orderRepository.findByStatus(status, pageable);
		} else if (keyword != null && !keyword.trim().isEmpty()) {
			return orderRepository.findByKeyword(keyword.trim(), pageable);
		} else {
			return orderRepository.findAll(pageable);
		}
	}

	@Override
	public List<Orders> findByCustomerId(Long customerId) {
		// TODO Auto-generated method stub
		return orderRepository.findByCustomerId(customerId);
	}
	
	@Override
	@Transactional
	public void bulkUpdateStatus(List<Long> orderIds, int status, String changedBy) {
		for (Long orderId : orderIds) {
			try {
				updateStatus(orderId, status, changedBy);
			} catch (Exception e) {
				// Log lỗi nhưng tiếp tục xử lý các đơn khác
				System.err.println("Lỗi khi cập nhật đơn hàng #" + orderId + ": " + e.getMessage());
			}
		}
	}
	
	@Override
	public List<ProductSalesDTO> getTopSellingProducts(int limit) {
		List<Object[]> results = orderRepository.getTopSellingProducts(limit);
		List<ProductSalesDTO> dtos = new ArrayList<>();
		
		for (Object[] row : results) {
			ProductSalesDTO dto = new ProductSalesDTO();
			dto.setProductId(((Number) row[0]).longValue());
			dto.setProductName((String) row[1]);
			dto.setTotalQuantitySold(((Number) row[2]).longValue());
			dto.setTotalRevenue(((Number) row[3]).doubleValue());
			dtos.add(dto);
		}
		
		return dtos;
	}
	
	@Override
	public List<OrderStatisticsDTO> getOrderStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
		List<Object[]> results = orderRepository.getOrderStatisticsByDateRange(
			java.sql.Date.valueOf(startDate),
			java.sql.Date.valueOf(endDate)
		);
		
		List<OrderStatisticsDTO> dtos = new ArrayList<>();
		for (Object[] row : results) {
			OrderStatisticsDTO dto = new OrderStatisticsDTO();
			dto.setDate(((java.sql.Date) row[0]).toLocalDate());
			dto.setOrderCount(((Number) row[1]).longValue());
			dto.setTotalRevenue(row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
			dtos.add(dto);
		}
		
		return dtos;
	}
	
	@Override
	public double getCancellationRate() {
		long totalOrders = orderRepository.count();
		if (totalOrders == 0) {
			return 0.0;
		}
		
		long cancelledOrders = countByStatus(OrderStatusValidator.STATUS_CANCELLED);
		return (cancelledOrders * 100.0) / totalOrders;
	}
	
	@Override
	public long countOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
		return orderRepository.countOrdersByDateRange(
			java.sql.Date.valueOf(startDate),
			java.sql.Date.valueOf(endDate)
		);
	}
	
	@Override
	public double getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
		Double revenue = orderRepository.getRevenueByDateRange(
			java.sql.Date.valueOf(startDate),
			java.sql.Date.valueOf(endDate)
		);
		return revenue != null ? revenue : 0.0;
	}

	
	@Override
	public List<OrderStatusHistory> getStatusHistory(Long orderId) {
		return statusHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId);
	}
	
	@Override
	@Transactional
	public OrderNote addNote(Long orderId, String content, String createdBy, Boolean isInternal) {
		Orders order = findById(orderId);
		if (order == null) {
			throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
		}
		
		OrderNote note = new OrderNote(order, content, createdBy, isInternal);
		return noteRepository.save(note);
	}
	
	@Override
	public List<OrderNote> getNotes(Long orderId) {
		return noteRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
	}
	
	@Override
	public List<OrderNote> getInternalNotes(Long orderId) {
		return noteRepository.findByOrderIdAndIsInternalOrderByCreatedAtDesc(orderId, true);
	}
}

package com.bkap.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bkap.dto.OrderStatisticsDTO;
import com.bkap.dto.ProductSalesDTO;
import com.bkap.entity.Customer;
import com.bkap.entity.OrderNote;
import com.bkap.entity.OrderStatusHistory;
import com.bkap.entity.Orders;
import com.bkap.entity.Review;

@Service
public interface OrderService {
	List<Orders> findAll();

	Orders findById(Long id);

	Orders save(Orders order);
	
	Boolean create(Orders order);
	
	Boolean createOrderDetail(com.bkap.entity.OrderDetail orderDetail);

	void deleteById(Long id);

	// Dashboard
	long countTodayOrders();

	double getTodayRevenue();

	// Tìm kiếm & lọc
	List<Orders> searchOrders(String keyword, Integer status);

	List<Orders> getOrderByUser(Customer customer);

	List<Orders> getOrdersByUserAndStatus(Customer customer, Integer status);

	// Order
	void updateStatus(Long id, int status);
	
	void updateStatus(Long id, int status, String changedBy);

	long countByStatus(int status);

	// ➕ Thêm mới
	Page<Orders> findByFilter(Integer status, String keyword, Pageable pageable);
	
	//Xem đơn hàng
	List<Orders> findByCustomerId(Long customerId);
	
	// Bulk actions
	void bulkUpdateStatus(List<Long> orderIds, int status, String changedBy);
	
	// Thống kê
	List<ProductSalesDTO> getTopSellingProducts(int limit);
	
	List<OrderStatisticsDTO> getOrderStatisticsByDateRange(LocalDate startDate, LocalDate endDate);
	
	double getCancellationRate();
	
	long countOrdersByDateRange(LocalDate startDate, LocalDate endDate);
	
	double getRevenueByDateRange(LocalDate startDate, LocalDate endDate);
	
	// Lịch sử trạng thái
	List<OrderStatusHistory> getStatusHistory(Long orderId);
	
	// Ghi chú
	OrderNote addNote(Long orderId, String content, String createdBy, Boolean isInternal);
	
	List<OrderNote> getNotes(Long orderId);
	
	List<OrderNote> getInternalNotes(Long orderId);
}

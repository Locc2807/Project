package com.bkap.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bkap.entity.Customer;
import com.bkap.entity.Orders;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

	// Thống kê doanh thu theo tháng (native OK)
	@Query(value = "SELECT TO_CHAR(o.created, 'MM') AS month, " + "SUM(od.price * od.quantity) AS total "
			+ "FROM orders o " + "JOIN order_details od ON o.id = od.order_id " + "GROUP BY TO_CHAR(o.created, 'MM') "
			+ "ORDER BY TO_CHAR(o.created, 'MM')", nativeQuery = true)
	List<Object[]> getRevenueByMonth();

	// Đếm đơn hàng theo tháng (native OK)
	@Query(value = "SELECT TO_CHAR(created, 'MM') AS month, COUNT(*) AS total " + "FROM orders "
			+ "GROUP BY TO_CHAR(created, 'MM') " + "ORDER BY TO_CHAR(created, 'MM')", nativeQuery = true)
	List<Object[]> getOrdersByMonth();

	// Lịch sử giao dịch (native OK)
	@Query(value = "SELECT o.id, o.created, SUM(od.quantity * od.price), "
			+ "CASE WHEN o.status = 1 THEN 'Hoàn tất' ELSE 'Thất bại' END "
			+ "FROM orders o JOIN order_details od ON o.id = od.order_id " + "GROUP BY o.id, o.created, o.status "
			+ "ORDER BY o.created DESC", nativeQuery = true)
	List<Object[]> getTransactionHistory();

	// Tìm đơn hàng theo status và keyword (dùng trong filter)
	@Query("SELECT o FROM Orders o WHERE o.status = :status AND ("
			+ "LOWER(o.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
			+ "OR CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%')) " + "ORDER BY o.created DESC")
	Page<Orders> findByStatusAndKeyword(@Param("status") Integer status, @Param("keyword") String keyword,
			Pageable pageable);

	// Tìm theo status
	@Query("SELECT o FROM Orders o WHERE o.status = :status ORDER BY o.created DESC")
	Page<Orders> findByStatus(@Param("status") Integer status, Pageable pageable);

	// Tìm theo keyword
	@Query("SELECT o FROM Orders o WHERE " + "LOWER(o.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
			+ "OR CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%') " + "ORDER BY o.created DESC")
	Page<Orders> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

	// Tìm tất cả đơn theo khách hàng
	List<Orders> findByCustomer(Customer customer);

	// Tìm đơn theo khách và trạng thái
	List<Orders> findByCustomerAndStatus(Customer customer, Integer status);

	// Đếm số đơn trong ngày
	@Query(value = "SELECT COUNT(*) FROM orders o WHERE TRUNC(o.created) = TRUNC(SYSDATE)", nativeQuery = true)
	long countTodayOrders();

	// Tính tổng doanh thu trong ngày
	@Query(value = "SELECT SUM(od.price * od.quantity) FROM orders o " + "JOIN order_details od ON o.id = od.order_id "
			+ "WHERE TRUNC(o.created) = TRUNC(SYSDATE)", nativeQuery = true)
	Double getTodayRevenue();

	// Đếm theo trạng thái
	@Query(value = "SELECT COUNT(*) FROM orders o WHERE o.status = :status", nativeQuery = true)
	int countByStatus(@Param("status") int status);

	// Tìm đơn theo ID khách hàng (dùng ở admin/customer-orders)
	List<Orders> findByCustomerId(Long customerId);

	@Query("SELECT o FROM Orders o WHERE " + "(:status IS NULL OR o.status = :status) AND (" + ":keyword IS NULL OR "
			+ "LOWER(o.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(o.customer.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	List<Orders> searchOrders(@Param("keyword") String keyword, @Param("status") Integer status);
	
	// Thống kê sản phẩm bán chạy
	@Query(value = "SELECT p.id, p.name, SUM(od.quantity) as total_quantity, " +
			"SUM(od.price * od.quantity) as total_revenue " +
			"FROM products p " +
			"JOIN order_details od ON p.id = od.product_id " +
			"JOIN orders o ON od.order_id = o.id " +
			"WHERE o.status = 6 " + // Chỉ tính đơn thành công
			"GROUP BY p.id, p.name " +
			"ORDER BY total_quantity DESC " +
			"FETCH FIRST :limit ROWS ONLY", nativeQuery = true)
	List<Object[]> getTopSellingProducts(@Param("limit") int limit);
	
	// Thống kê đơn hàng theo khoảng thời gian
	@Query(value = "SELECT TRUNC(o.created) as order_date, " +
			"COUNT(*) as order_count, " +
			"SUM(od.price * od.quantity) as total_revenue " +
			"FROM orders o " +
			"JOIN order_details od ON o.id = od.order_id " +
			"WHERE TRUNC(o.created) BETWEEN :startDate AND :endDate " +
			"GROUP BY TRUNC(o.created) " +
			"ORDER BY order_date", nativeQuery = true)
	List<Object[]> getOrderStatisticsByDateRange(
		@Param("startDate") java.sql.Date startDate,
		@Param("endDate") java.sql.Date endDate
	);
	
	// Đếm đơn hàng theo khoảng thời gian
	@Query(value = "SELECT COUNT(*) FROM orders o " +
			"WHERE TRUNC(o.created) BETWEEN :startDate AND :endDate", nativeQuery = true)
	long countOrdersByDateRange(
		@Param("startDate") java.sql.Date startDate,
		@Param("endDate") java.sql.Date endDate
	);
	
	// Tính doanh thu theo khoảng thời gian
	@Query(value = "SELECT SUM(od.price * od.quantity) " +
			"FROM orders o " +
			"JOIN order_details od ON o.id = od.order_id " +
			"WHERE TRUNC(o.created) BETWEEN :startDate AND :endDate " +
			"AND o.status = 6", nativeQuery = true)
	Double getRevenueByDateRange(
		@Param("startDate") java.sql.Date startDate,
		@Param("endDate") java.sql.Date endDate
	);

}

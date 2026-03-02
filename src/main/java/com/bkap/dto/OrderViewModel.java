package com.bkap.dto;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.bkap.entity.Orders;

public class OrderViewModel {

	private final Orders order;
	private final double totalPrice;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

	public OrderViewModel(Orders order) {
		this.order = order;
		this.totalPrice = order.getOrderDetails().stream().mapToDouble(d -> d.getPrice() * d.getQuantity()).sum();
	}

	public Orders getOrder() {
		return order;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public String getFormattedTotalPrice() {
		return CURRENCY_FORMATTER.format(totalPrice); // Ví dụ: 1.200.000 ₫
	}

	public String getFormattedCreatedDate() {
		if (order.getCreated() == null)
			return "";
		return order.getCreated().format(DATE_FORMATTER);
	}

	public String getStatusText() {
		if (order == null || order.getStatus() == null) {
			return "Không xác định";
		}

		switch (order.getStatus()) {
		case 1:
			return "Chờ xác nhận";
		case 2:
			return "Đã xác nhận";
		case 3:
			return "Đang chuyển";
		case 4:
			return "Đang giao";
		case 5:
			return "Đã huỷ";
		case 6:
			return "Thành công";
		case 7:
			return "Yêu cầu huỷ";
		default:
			return "Không rõ";
		}
	}
	
	/**
	 * Format số tiền thành chuỗi tiền tệ VN
	 */
	public String formatCurrency(double amount) {
		return CURRENCY_FORMATTER.format(amount);
	}

}

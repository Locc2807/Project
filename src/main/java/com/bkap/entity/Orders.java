package com.bkap.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Orders {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
	private Customer customer;

	@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
	private List<OrderDetail> orderDetails;
	
	@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
	private List<OrderStatusHistory> statusHistories;
	
	@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
	private List<OrderNote> notes;

	@Column(name = "order_note")
	private String order_note;
	
	@Column(name = "created")
	private LocalDateTime created;
	
	@Column(name = "status")
	private Integer status;
	
	// Các cột mới
	@Column(name = "order_code", unique = true, length = 50)
	private String orderCode;
	
	@Column(name = "total_amount")
	private Double totalAmount;
	
	@Column(name = "shipping_fee")
	private Double shippingFee;
	
	@Column(name = "shipping_address", length = 500)
	private String shippingAddress;
	
	@Column(name = "receiver_name", length = 100)
	private String receiverName;
	
	@Column(name = "receiver_phone", length = 20)
	private String receiverPhone;
	
	@Column(name = "payment_method", length = 50)
	private String paymentMethod;
	
	@Column(name = "is_paid")
	private Boolean isPaid;
	
	@Column(name = "paid_at")
	private LocalDateTime paidAt;
	
	@Column(name = "confirmed_at")
	private LocalDateTime confirmedAt;
	
	@Column(name = "shipped_at")
	private LocalDateTime shippedAt;
	
	@Column(name = "delivered_at")
	private LocalDateTime deliveredAt;
	
	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;
	
	@Column(name = "cancel_reason", length = 500)
	private String cancelReason;
	
	// Getters and Setters
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Customer getCustomer() {
		return customer;
	}
	
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	public List<OrderDetail> getOrderDetails() {
		return orderDetails;
	}
	
	public void setOrderDetails(List<OrderDetail> orderDetails) {
		this.orderDetails = orderDetails;
	}
	
	public List<OrderStatusHistory> getStatusHistories() {
		return statusHistories;
	}
	
	public void setStatusHistories(List<OrderStatusHistory> statusHistories) {
		this.statusHistories = statusHistories;
	}
	
	public List<OrderNote> getNotes() {
		return notes;
	}
	
	public void setNotes(List<OrderNote> notes) {
		this.notes = notes;
	}
	
	public String getOrder_note() {
		return order_note;
	}
	
	public void setOrder_note(String order_note) {
		this.order_note = order_note;
	}
	
	public LocalDateTime getCreated() {
		return created;
	}
	
	public void setCreated(LocalDateTime created) {
		this.created = created;
	}
	
	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public String getOrderCode() {
		return orderCode;
	}
	
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	
	public Double getTotalAmount() {
		return totalAmount;
	}
	
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	
	public Double getShippingFee() {
		return shippingFee;
	}
	
	public void setShippingFee(Double shippingFee) {
		this.shippingFee = shippingFee;
	}
	
	public String getShippingAddress() {
		return shippingAddress;
	}
	
	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}
	
	public String getReceiverName() {
		return receiverName;
	}
	
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
	
	public String getReceiverPhone() {
		return receiverPhone;
	}
	
	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}
	
	public String getPaymentMethod() {
		return paymentMethod;
	}
	
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	
	public Boolean getIsPaid() {
		return isPaid;
	}
	
	public void setIsPaid(Boolean isPaid) {
		this.isPaid = isPaid;
	}
	
	public LocalDateTime getPaidAt() {
		return paidAt;
	}
	
	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}
	
	public LocalDateTime getConfirmedAt() {
		return confirmedAt;
	}
	
	public void setConfirmedAt(LocalDateTime confirmedAt) {
		this.confirmedAt = confirmedAt;
	}
	
	public LocalDateTime getShippedAt() {
		return shippedAt;
	}
	
	public void setShippedAt(LocalDateTime shippedAt) {
		this.shippedAt = shippedAt;
	}
	
	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}
	
	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}
	
	public LocalDateTime getCancelledAt() {
		return cancelledAt;
	}
	
	public void setCancelledAt(LocalDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}
	
	public String getCancelReason() {
		return cancelReason;
	}
	
	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}
}

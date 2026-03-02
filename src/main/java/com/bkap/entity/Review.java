package com.bkap.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviewers")
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Column(name = "star_value")
	private Integer starValue;

	@Column(name = "content")
	private String content;

	@Column(name = "created")
	private LocalDateTime created;

	@PrePersist
	protected void onCreate() {
		this.created = LocalDateTime.now();
	}

	public Review() {
		// default constructor
	}

	public Review(Long id, Product product, Customer customer, Integer starValue, String content,
			LocalDateTime created) {
		super();
		this.id = id;
		this.product = product;
		this.customer = customer;
		this.starValue = starValue;
		this.content = content;
		this.created = created;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Integer getStarValue() {
		return starValue;
	}

	public void setStarValue(Integer starValue) {
		this.starValue = starValue;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

}

package com.bkap.model;

public class CartItem {
	private Long id;
	private String name;
	private String image;
	private Float price; // price, sale_price
	private int quantity;

	public CartItem() {
		// TODO Auto-generated constructor stub
	}

	public CartItem(Long id, String name, String image, Float price, int quantity) {
		super();
		this.id = id;
		this.name = name;
		this.image = image;
		this.price = price;
		this.quantity = quantity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getTotalPrice() {
		return price * quantity;
	}
}

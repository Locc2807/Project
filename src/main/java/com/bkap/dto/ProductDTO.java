package com.bkap.dto;

import com.bkap.entity.Product;

public class ProductDTO {
	private Long id;
	private String name;
	private Double price;
	private String image;
	private String description;
	private String categoryName;

	public ProductDTO(Product p) {
		this.id = p.getId();
		this.name = p.getName();
		this.price = p.getPrice();
		this.image = p.getImage();
		this.description = p.getDescription(); // bạn thiếu dòng này
		this.categoryName = (p.getCategory() != null) ? p.getCategory().getName() : "";
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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	
}

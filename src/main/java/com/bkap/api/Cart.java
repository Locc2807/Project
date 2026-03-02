package com.bkap.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bkap.entity.Product;
import com.bkap.model.CartItem;

public class Cart {

	private Map<Long, CartItem> items = new HashMap<>();

	// Thêm sản phẩm vào giỏ
	public void addItem(Product product) {
		addItem(product, 1);
	}

	// Thêm sản phẩm với số lượng
	public void addItem(Product product, int quantity) {
		Long id = product.getId();
		if (items.containsKey(id)) {
			CartItem item = items.get(id);
			item.setQuantity(item.getQuantity() + quantity);
		} else {
			CartItem item = new CartItem();
			item.setId(product.getId());
			item.setName(product.getName());
			item.setImage(product.getImage());
			item.setPrice(product.getPrice().floatValue());
			item.setQuantity(quantity);
			items.put(id, item);
		}
	}

	// Cập nhật số lượng
	public void updateQuantity(Long productId, int quantity) {
		if (items.containsKey(productId)) {
			if (quantity <= 0) {
				items.remove(productId);
			} else {
				items.get(productId).setQuantity(quantity);
			}
		}
	}

	// Xóa sản phẩm khỏi giỏ
	public void removeItem(Long productId) {
		items.remove(productId);
	}

	// Xóa toàn bộ giỏ hàng
	public void clear() {
		items.clear();
	}

	// Lấy tổng số sản phẩm
	public int getTotalItems() {
		return items.values().stream().mapToInt(CartItem::getQuantity).sum();
	}

	// Lấy tổng tiền
	public double getTotalPrice() {
		return items.values().stream()
				.mapToDouble(CartItem::getTotalPrice)
				.sum();
	}

	// Lấy danh sách items
	public List<CartItem> getCartItems() {
		return new ArrayList<>(items.values());
	}

	// Lấy map items (để tương thích với code cũ)
	public Map<Long, CartItem> getItems() {
		return items;
	}

	// Kiểm tra giỏ hàng có rỗng không
	public boolean isEmpty() {
		return items.isEmpty();
	}

	// Lấy số lượng loại sản phẩm
	public int getItemCount() {
		return items.size();
	}
}

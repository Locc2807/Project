# 🛒 HƯỚNG DẪN HOÀN THIỆN GIỎ HÀNG VÀ THANH TOÁN

## ✅ PHẦN 1: GIỎ HÀNG (CART) - ĐÃ HOÀN THÀNH

### Đã implement:

#### 1. Cart.java - Cải tiến
**Chức năng mới:**
- `addItem(Product, quantity)` - Thêm sản phẩm với số lượng
- `updateQuantity(productId, quantity)` - Cập nhật số lượng
- `removeItem(productId)` - Xóa sản phẩm
- `clear()` - Xóa toàn bộ giỏ
- `getTotalPrice()` - Tính tổng tiền
- `getCartItems()` - Lấy danh sách CartItem
- `isEmpty()` - Kiểm tra giỏ rỗng

#### 2. CartController.java - Cải tiến
**API Endpoints:**
- `POST /cart/add/{id}?quantity=1` - Thêm vào giỏ
- `POST /cart/update/{id}?quantity=2` - Cập nhật số lượng
- `POST /cart/remove/{id}` - Xóa khỏi giỏ
- `POST /cart/clear` - Xóa toàn bộ
- `GET /cart` - Xem giỏ hàng

#### 3. cart.html - Trang giỏ hàng
**Tính năng:**
- Hiển thị danh sách sản phẩm trong giỏ
- Tăng/giảm số lượng
- Xóa sản phẩm
- Xóa toàn bộ giỏ
- Tính tổng tiền tự động
- Nút "Thanh toán"
- Empty state khi giỏ rỗng

---

## ✅ PHẦN 2: THANH TOÁN (CHECKOUT) - ĐÃ HOÀN THÀNH

### Đã implement:

#### 1. CheckoutController.java
**Endpoints:**
- `GET /checkout` - Trang thanh toán
- `POST /checkout/place-order` - Xử lý đặt hàng
- `GET /checkout/success` - Trang đặt hàng thành công

**Logic xử lý đặt hàng:**
1. Validate giỏ hàng không rỗng
2. Tạo hoặc lấy Customer từ email
3. Tạo Order với đầy đủ thông tin
4. Tạo OrderDetails cho từng sản phẩm
5. Trừ tồn kho tự động (Inventory)
6. Xóa giỏ hàng
7. Redirect đến trang thành công

#### 2. checkout.html - Trang thanh toán
**Sections:**
- **Thông tin người nhận:**
  - Họ tên (required)
  - Số điện thoại (required)
  - Email (required)
  - Địa chỉ giao hàng (required)
  - Ghi chú đơn hàng (optional)

- **Phương thức thanh toán:**
  - COD (Thanh toán khi nhận hàng) ✅
  - Chuyển khoản ngân hàng ✅
  - Thẻ tín dụng (Coming soon)

- **Tóm tắt đơn hàng:**
  - Danh sách sản phẩm
  - Tạm tính
  - Phí vận chuyển (Miễn phí)
  - Tổng cộng

#### 3. checkout-success.html - Trang thành công
**Hiển thị:**
- Icon thành công
- Thông báo đặt hàng thành công
- Mã đơn hàng (orderCode)
- Thông tin đơn hàng
- Nút "Xem đơn hàng của tôi"
- Nút "Về trang chủ"

#### 4. Cập nhật Services
**CustomerService:**
- Thêm method `create(Customer)` để tạo khách hàng mới

**OrderService:**
- Thêm method `create(Orders)` để tạo đơn hàng
- Thêm method `createOrderDetail(OrderDetail)` để tạo chi tiết đơn

---

## 🔄 WORKFLOW ĐẶT HÀNG

### Bước 1: Thêm sản phẩm vào giỏ
1. User click "Thêm vào giỏ" ở trang sản phẩm
2. AJAX call `POST /cart/add/{id}`
3. Session lưu Cart object
4. Icon giỏ hàng cập nhật số lượng

### Bước 2: Xem giỏ hàng
1. User click icon giỏ hàng hoặc vào `/cart`
2. Hiển thị danh sách sản phẩm
3. User có thể:
   - Tăng/giảm số lượng
   - Xóa sản phẩm
   - Xóa toàn bộ giỏ

### Bước 3: Thanh toán
1. User click "Thanh toán" → `/checkout`
2. Điền form thông tin:
   - Thông tin người nhận
   - Địa chỉ giao hàng
   - Phương thức thanh toán
3. Click "Đặt hàng"

### Bước 4: Xử lý đơn hàng (Backend)
1. Validate giỏ hàng
2. Tạo/Lấy Customer
3. Tạo Order (status = 1 - Chờ xác nhận)
4. Tạo OrderDetails
5. Trừ tồn kho (InventoryService.adjustStock)
6. Xóa giỏ hàng
7. Redirect `/checkout/success`

### Bước 5: Thành công
1. Hiển thị mã đơn hàng
2. Gửi email xác nhận (optional)
3. User có thể xem đơn hàng tại `/user/orders`

---

## 📁 FILES ĐÃ TẠO/SỬA

### Backend:
1. **Cart.java** - Cải tiến logic giỏ hàng
2. **CartController.java** - API giỏ hàng + trang cart
3. **CheckoutController.java** - Xử lý checkout (NEW)
4. **CustomerService.java** - Thêm method create
5. **CustomerServiceImpl.java** - Implement create
6. **OrderService.java** - Thêm methods create, createOrderDetail
7. **OrderServiceImpl.java** - Implement methods

### Frontend:
1. **cart.html** - Trang giỏ hàng (NEW)
2. **checkout.html** - Trang thanh toán (NEW)
3. **checkout-success.html** - Trang thành công (NEW)

---

## 🧪 TESTING CHECKLIST

### Giỏ hàng:
- [ ] Thêm sản phẩm vào giỏ
- [ ] Xem giỏ hàng
- [ ] Tăng/giảm số lượng
- [ ] Xóa sản phẩm
- [ ] Xóa toàn bộ giỏ
- [ ] Tính tổng tiền đúng
- [ ] Empty state khi giỏ rỗng

### Thanh toán:
- [ ] Vào trang checkout
- [ ] Điền form thông tin
- [ ] Chọn phương thức thanh toán
- [ ] Đặt hàng thành công
- [ ] Order được tạo trong database
- [ ] OrderDetails được tạo
- [ ] Tồn kho bị trừ
- [ ] Giỏ hàng bị xóa
- [ ] Hiển thị trang thành công
- [ ] Mã đơn hàng hiển thị đúng

---

## 🎯 CẦN LÀM THÊM (OPTIONAL)

### 1. Cập nhật Header
Thêm icon giỏ hàng với số lượng:
```html
<a href="/cart" class="relative">
    <i class="fa-solid fa-shopping-cart"></i>
    <span class="badge" th:text="${session.cart != null ? session.cart.totalItems : 0}"></span>
</a>
```

### 2. Nút "Thêm vào giỏ" ở trang sản phẩm
```javascript
function addToCart(productId) {
    fetch(`/cart/add/${productId}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        alert('Đã thêm vào giỏ hàng!');
        // Cập nhật số lượng trên icon
        document.querySelector('.cart-count').textContent = data.totalItems;
    });
}
```

### 3. Gửi email xác nhận
Sử dụng MailService đã có để gửi email:
```java
mailService.sendOrderConfirmation(customer.getEmail(), order);
```

### 4. Validation tồn kho trước khi đặt hàng
Kiểm tra xem còn đủ hàng không:
```java
if (inventory.getQuantity() < item.getQuantity()) {
    throw new Exception("Sản phẩm " + product.getName() + " không đủ hàng");
}
```

---

## ✅ KẾT QUẢ

Đã hoàn thành đầy đủ 2 phần:
- ✅ Giỏ hàng (Cart) - Đầy đủ chức năng
- ✅ Thanh toán (Checkout) - Xử lý đặt hàng hoàn chỉnh

Bạn có thể:
1. **Restart ứng dụng**
2. **Test giỏ hàng:** Vào `/cart`
3. **Test thanh toán:** Thêm sản phẩm → Thanh toán → Đặt hàng
4. **Kiểm tra database:** Xem Orders, OrderDetails, Inventory

Sẵn sàng để test! 🎉

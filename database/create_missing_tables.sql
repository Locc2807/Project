-- =====================================================
-- SCRIPT TẠO CÁC BẢNG THIẾU CHO DỰ ÁN E-COMMERCE
-- Database: Oracle
-- Ngày tạo: 2026-02-05
-- =====================================================

-- =====================================================
-- 1. BẢNG PAYMENTS (Thanh toán)
-- =====================================================
CREATE TABLE payments (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id NUMBER NOT NULL,
    payment_method VARCHAR2(50) NOT NULL,
    amount NUMBER(10,2) NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING',
    transaction_id VARCHAR2(100),
    payment_gateway VARCHAR2(50),
    payment_date TIMESTAMP,
    vnpay_data CLOB,
    momo_data CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes cho performance
CREATE INDEX idx_payment_order ON payments(order_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_transaction ON payments(transaction_id);
CREATE INDEX idx_payment_date ON payments(payment_date);

COMMENT ON TABLE payments IS 'Bảng lưu thông tin thanh toán';
COMMENT ON COLUMN payments.payment_method IS 'COD, BANK_TRANSFER, VNPAY, MOMO, ZALOPAY';
COMMENT ON COLUMN payments.status IS 'PENDING, COMPLETED, FAILED, REFUNDED';

-- =====================================================
-- 2. BẢNG CARTS (Giỏ hàng persistent)
-- =====================================================
CREATE TABLE carts (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT uk_cart_customer UNIQUE (customer_id)
);

CREATE INDEX idx_cart_customer ON carts(customer_id);

COMMENT ON TABLE carts IS 'Bảng giỏ hàng của khách hàng';

-- =====================================================
-- 3. BẢNG CART_ITEMS (Chi tiết giỏ hàng)
-- =====================================================
CREATE TABLE cart_items (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cart_id NUMBER NOT NULL,
    product_id NUMBER NOT NULL,
    quantity NUMBER DEFAULT 1 CHECK (quantity > 0),
    price NUMBER(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_cart_item_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_item_product ON cart_items(product_id);

COMMENT ON TABLE cart_items IS 'Chi tiết sản phẩm trong giỏ hàng';

-- =====================================================
-- 4. BẢNG INVENTORY (Quản lý tồn kho)
-- =====================================================
CREATE TABLE inventory (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id NUMBER NOT NULL UNIQUE,
    quantity NUMBER DEFAULT 0 CHECK (quantity >= 0),
    reserved_quantity NUMBER DEFAULT 0 CHECK (reserved_quantity >= 0),
    low_stock_threshold NUMBER DEFAULT 10,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_inventory_product ON inventory(product_id);
CREATE INDEX idx_inventory_quantity ON inventory(quantity);

COMMENT ON TABLE inventory IS 'Quản lý tồn kho sản phẩm';
COMMENT ON COLUMN inventory.reserved_quantity IS 'Số lượng đã đặt nhưng chưa thanh toán';

-- =====================================================
-- 5. BẢNG SHIPPING_ADDRESSES (Địa chỉ giao hàng)
-- =====================================================
CREATE TABLE shipping_addresses (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER NOT NULL,
    recipient_name VARCHAR2(100) NOT NULL,
    phone VARCHAR2(20) NOT NULL,
    address VARCHAR2(500) NOT NULL,
    ward VARCHAR2(100),
    district VARCHAR2(100),
    city VARCHAR2(100) NOT NULL,
    is_default NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shipping_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE INDEX idx_shipping_customer ON shipping_addresses(customer_id);
CREATE INDEX idx_shipping_default ON shipping_addresses(customer_id, is_default);

COMMENT ON TABLE shipping_addresses IS 'Địa chỉ giao hàng của khách hàng';
COMMENT ON COLUMN shipping_addresses.is_default IS '1 = địa chỉ mặc định, 0 = không';

-- =====================================================
-- 6. BẢNG COUPONS (Mã giảm giá)
-- =====================================================
CREATE TABLE coupons (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR2(50) UNIQUE NOT NULL,
    discount_type VARCHAR2(20) NOT NULL,
    discount_value NUMBER(10,2) NOT NULL,
    min_order_value NUMBER(10,2) DEFAULT 0,
    max_discount NUMBER(10,2),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    usage_limit NUMBER,
    used_count NUMBER DEFAULT 0,
    status NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_coupon_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_coupon_usage CHECK (used_count <= usage_limit OR usage_limit IS NULL)
);

CREATE INDEX idx_coupon_code ON coupons(code);
CREATE INDEX idx_coupon_status ON coupons(status);
CREATE INDEX idx_coupon_dates ON coupons(start_date, end_date);

COMMENT ON TABLE coupons IS 'Mã giảm giá';
COMMENT ON COLUMN coupons.discount_type IS 'PERCENT hoặc FIXED';
COMMENT ON COLUMN coupons.status IS '1 = active, 0 = inactive';

-- =====================================================
-- 7. BẢNG WISHLISTS (Danh sách yêu thích)
-- =====================================================
CREATE TABLE wishlists (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER NOT NULL,
    product_id NUMBER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wishlist_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uk_wishlist_customer_product UNIQUE (customer_id, product_id)
);

CREATE INDEX idx_wishlist_customer ON wishlists(customer_id);
CREATE INDEX idx_wishlist_product ON wishlists(product_id);

COMMENT ON TABLE wishlists IS 'Danh sách sản phẩm yêu thích của khách hàng';

-- =====================================================
-- 8. BẢNG ORDER_COUPONS (Liên kết đơn hàng và coupon)
-- =====================================================
CREATE TABLE order_coupons (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id NUMBER NOT NULL,
    coupon_id NUMBER NOT NULL,
    discount_amount NUMBER(10,2) NOT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_coupon_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_coupon_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_coupon_order ON order_coupons(order_id);
CREATE INDEX idx_order_coupon_coupon ON order_coupons(coupon_id);

COMMENT ON TABLE order_coupons IS 'Lưu thông tin coupon đã áp dụng cho đơn hàng';

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Trigger cập nhật updated_at cho carts
CREATE OR REPLACE TRIGGER trg_carts_updated_at
BEFORE UPDATE ON carts
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Trigger cập nhật updated_at cho shipping_addresses
CREATE OR REPLACE TRIGGER trg_shipping_updated_at
BEFORE UPDATE ON shipping_addresses
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Trigger cập nhật updated_at cho coupons
CREATE OR REPLACE TRIGGER trg_coupons_updated_at
BEFORE UPDATE ON coupons
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Trigger cập nhật last_updated cho inventory
CREATE OR REPLACE TRIGGER trg_inventory_updated
BEFORE UPDATE ON inventory
FOR EACH ROW
BEGIN
    :NEW.last_updated := CURRENT_TIMESTAMP;
END;
/

-- Trigger đảm bảo chỉ có 1 địa chỉ mặc định
CREATE OR REPLACE TRIGGER trg_shipping_default
BEFORE INSERT OR UPDATE ON shipping_addresses
FOR EACH ROW
BEGIN
    IF :NEW.is_default = 1 THEN
        UPDATE shipping_addresses 
        SET is_default = 0 
        WHERE customer_id = :NEW.customer_id 
        AND id != :NEW.id;
    END IF;
END;
/

-- =====================================================
-- DỮ LIỆU MẪU (OPTIONAL)
-- =====================================================

-- Thêm inventory cho các sản phẩm hiện có
INSERT INTO inventory (product_id, quantity, reserved_quantity, low_stock_threshold)
SELECT id, 100, 0, 10 FROM products;

-- Thêm coupon mẫu
INSERT INTO coupons (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, usage_limit, status)
VALUES ('WELCOME10', 'PERCENT', 10, 0, 100000, SYSDATE, ADD_MONTHS(SYSDATE, 3), 100, 1);

INSERT INTO coupons (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, usage_limit, status)
VALUES ('FREESHIP', 'FIXED', 30000, 200000, 30000, SYSDATE, ADD_MONTHS(SYSDATE, 1), 50, 1);

COMMIT;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Kiểm tra các bảng đã được tạo
SELECT table_name FROM user_tables 
WHERE table_name IN ('PAYMENTS', 'CARTS', 'CART_ITEMS', 'INVENTORY', 
                     'SHIPPING_ADDRESSES', 'COUPONS', 'WISHLISTS', 'ORDER_COUPONS')
ORDER BY table_name;

-- Kiểm tra số lượng records
SELECT 'PAYMENTS' as table_name, COUNT(*) as count FROM payments
UNION ALL
SELECT 'CARTS', COUNT(*) FROM carts
UNION ALL
SELECT 'CART_ITEMS', COUNT(*) FROM cart_items
UNION ALL
SELECT 'INVENTORY', COUNT(*) FROM inventory
UNION ALL
SELECT 'SHIPPING_ADDRESSES', COUNT(*) FROM shipping_addresses
UNION ALL
SELECT 'COUPONS', COUNT(*) FROM coupons
UNION ALL
SELECT 'WISHLISTS', COUNT(*) FROM wishlists
UNION ALL
SELECT 'ORDER_COUPONS', COUNT(*) FROM order_coupons;

-- =====================================================
-- CLEANUP SCRIPT (Nếu cần xóa và tạo lại)
-- =====================================================
/*
DROP TABLE order_coupons CASCADE CONSTRAINTS;
DROP TABLE wishlists CASCADE CONSTRAINTS;
DROP TABLE coupons CASCADE CONSTRAINTS;
DROP TABLE shipping_addresses CASCADE CONSTRAINTS;
DROP TABLE inventory CASCADE CONSTRAINTS;
DROP TABLE cart_items CASCADE CONSTRAINTS;
DROP TABLE carts CASCADE CONSTRAINTS;
DROP TABLE payments CASCADE CONSTRAINTS;
*/

-- =====================================================
-- KẾT THÚC SCRIPT
-- =====================================================

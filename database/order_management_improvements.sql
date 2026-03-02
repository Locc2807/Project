-- =====================================================
-- SCRIPT CẢI TIẾN QUẢN LÝ ĐƠN HÀNG
-- Database: Oracle
-- Ngày tạo: 2026-03-02
-- Mô tả: Thêm các bảng và cột mới để cải tiến quản lý đơn hàng
-- =====================================================

-- =====================================================
-- BƯỚC 1: TẠO BẢNG ORDER_STATUS_HISTORY
-- Mục đích: Lưu lịch sử thay đổi trạng thái đơn hàng
-- =====================================================

CREATE TABLE order_status_history (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id NUMBER NOT NULL,
    old_status NUMBER,
    new_status NUMBER NOT NULL,
    changed_by VARCHAR2(100) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR2(500),
    CONSTRAINT fk_status_history_order 
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Tạo indexes để tăng performance
CREATE INDEX idx_status_history_order ON order_status_history(order_id);
CREATE INDEX idx_status_history_date ON order_status_history(changed_at);
CREATE INDEX idx_status_history_user ON order_status_history(changed_by);

-- Thêm comments
COMMENT ON TABLE order_status_history IS 'Lịch sử thay đổi trạng thái đơn hàng';
COMMENT ON COLUMN order_status_history.old_status IS 'Trạng thái cũ (1-7)';
COMMENT ON COLUMN order_status_history.new_status IS 'Trạng thái mới (1-7)';
COMMENT ON COLUMN order_status_history.changed_by IS 'Username của admin thực hiện thay đổi';
COMMENT ON COLUMN order_status_history.note IS 'Ghi chú lý do thay đổi';

-- =====================================================
-- BƯỚC 2: THÊM CỘT VÀO BẢNG ORDERS
-- Mục đích: Bổ sung thông tin quan trọng cho đơn hàng
-- =====================================================

-- Thêm các cột mới
ALTER TABLE orders ADD (
    order_code VARCHAR2(50),
    total_amount NUMBER(10,2),
    shipping_fee NUMBER(10,2) DEFAULT 0,
    shipping_address VARCHAR2(500),
    receiver_name VARCHAR2(100),
    receiver_phone VARCHAR2(20),
    payment_method VARCHAR2(50) DEFAULT 'COD',
    is_paid NUMBER(1) DEFAULT 0,
    paid_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR2(500)
);

-- Thêm constraint unique cho order_code (sẽ tự động tạo index)
ALTER TABLE orders ADD CONSTRAINT uk_orders_code UNIQUE (order_code);

-- Tạo indexes (bỏ qua order_code vì UNIQUE constraint đã tạo index rồi)
-- CREATE INDEX idx_orders_code ON orders(order_code); -- Không cần vì UNIQUE đã tạo
CREATE INDEX idx_orders_payment ON orders(payment_method);
CREATE INDEX idx_orders_paid ON orders(is_paid);
CREATE INDEX idx_orders_receiver_phone ON orders(receiver_phone);

-- Thêm comments
COMMENT ON COLUMN orders.order_code IS 'Mã đơn hàng dạng ORD20260302001';
COMMENT ON COLUMN orders.total_amount IS 'Tổng tiền đơn hàng (bao gồm phí ship)';
COMMENT ON COLUMN orders.shipping_fee IS 'Phí vận chuyển';
COMMENT ON COLUMN orders.shipping_address IS 'Địa chỉ giao hàng đầy đủ';
COMMENT ON COLUMN orders.receiver_name IS 'Tên người nhận hàng';
COMMENT ON COLUMN orders.receiver_phone IS 'Số điện thoại người nhận';
COMMENT ON COLUMN orders.payment_method IS 'COD, BANK_TRANSFER, VNPAY, MOMO';
COMMENT ON COLUMN orders.is_paid IS '1 = đã thanh toán, 0 = chưa thanh toán';
COMMENT ON COLUMN orders.paid_at IS 'Thời điểm thanh toán';
COMMENT ON COLUMN orders.confirmed_at IS 'Thời điểm xác nhận đơn';
COMMENT ON COLUMN orders.shipped_at IS 'Thời điểm bắt đầu giao hàng';
COMMENT ON COLUMN orders.delivered_at IS 'Thời điểm giao hàng thành công';
COMMENT ON COLUMN orders.cancelled_at IS 'Thời điểm hủy đơn';
COMMENT ON COLUMN orders.cancel_reason IS 'Lý do hủy đơn';

-- =====================================================
-- BƯỚC 3: TẠO TRIGGER TỰ ĐỘNG TẠO ORDER_CODE
-- Mục đích: Tự động generate mã đơn hàng dạng ORD20260302001
-- =====================================================

CREATE OR REPLACE TRIGGER trg_orders_code
BEFORE INSERT ON orders
FOR EACH ROW
WHEN (NEW.order_code IS NULL)
DECLARE
    v_seq NUMBER;
    v_date_str VARCHAR2(8);
BEGIN
    -- Lấy ngày hiện tại dạng YYYYMMDD
    v_date_str := TO_CHAR(SYSDATE, 'YYYYMMDD');
    
    -- Tìm số thứ tự tiếp theo trong ngày
    SELECT NVL(MAX(TO_NUMBER(SUBSTR(order_code, 12))), 0) + 1 
    INTO v_seq
    FROM orders
    WHERE order_code LIKE 'ORD' || v_date_str || '%';
    
    -- Tạo order_code: ORD + YYYYMMDD + 001
    :NEW.order_code := 'ORD' || v_date_str || LPAD(v_seq, 3, '0');
END;
/

-- =====================================================
-- BƯỚC 4: CẬP NHẬT DỮ LIỆU CŨ
-- Mục đích: Tạo order_code cho các đơn hàng cũ
-- =====================================================

-- Tạo order_code cho đơn hàng cũ (nếu có)
UPDATE orders 
SET order_code = 'ORD' || TO_CHAR(created, 'YYYYMMDD') || LPAD(id, 3, '0')
WHERE order_code IS NULL;

-- Tính total_amount cho đơn hàng cũ
UPDATE orders o
SET total_amount = (
    SELECT SUM(od.price * od.quantity)
    FROM order_details od
    WHERE od.order_id = o.id
)
WHERE total_amount IS NULL;

COMMIT;

-- =====================================================
-- BƯỚC 5: TẠO BẢNG ORDER_NOTES
-- Mục đích: Lưu ghi chú nội bộ cho đơn hàng
-- =====================================================

CREATE TABLE order_notes (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id NUMBER NOT NULL,
    content VARCHAR2(1000) NOT NULL,
    created_by VARCHAR2(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_internal NUMBER(1) DEFAULT 1,
    CONSTRAINT fk_order_note_order 
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Tạo indexes
CREATE INDEX idx_order_note_order ON order_notes(order_id);
CREATE INDEX idx_order_note_date ON order_notes(created_at);
CREATE INDEX idx_order_note_user ON order_notes(created_by);

-- Thêm comments
COMMENT ON TABLE order_notes IS 'Ghi chú cho đơn hàng';
COMMENT ON COLUMN order_notes.content IS 'Nội dung ghi chú';
COMMENT ON COLUMN order_notes.created_by IS 'Username của người tạo ghi chú';
COMMENT ON COLUMN order_notes.is_internal IS '1 = chỉ admin thấy, 0 = khách hàng cũng thấy';

-- =====================================================
-- BƯỚC 6: KIỂM TRA KẾT QUẢ
-- =====================================================

-- Kiểm tra các bảng đã được tạo
SELECT table_name, num_rows 
FROM user_tables 
WHERE table_name IN ('ORDER_STATUS_HISTORY', 'ORDER_NOTES')
ORDER BY table_name;

-- Kiểm tra các cột mới trong bảng ORDERS
SELECT column_name, data_type, data_length, nullable
FROM user_tab_columns
WHERE table_name = 'ORDERS'
AND column_name IN ('ORDER_CODE', 'TOTAL_AMOUNT', 'SHIPPING_FEE', 
                    'SHIPPING_ADDRESS', 'RECEIVER_NAME', 'RECEIVER_PHONE',
                    'PAYMENT_METHOD', 'IS_PAID')
ORDER BY column_name;

-- Kiểm tra trigger
SELECT trigger_name, status
FROM user_triggers
WHERE trigger_name = 'TRG_ORDERS_CODE';

-- Kiểm tra indexes
SELECT index_name, table_name, uniqueness
FROM user_indexes
WHERE table_name IN ('ORDERS', 'ORDER_STATUS_HISTORY', 'ORDER_NOTES')
ORDER BY table_name, index_name;

-- =====================================================
-- BƯỚC 7: TEST DỮ LIỆU MẪU (OPTIONAL)
-- =====================================================

-- Test tạo đơn hàng mới với order_code tự động
-- INSERT INTO orders (customer_id, order_note, created, status)
-- VALUES (1, 'Test order', CURRENT_TIMESTAMP, 1);

-- Kiểm tra order_code đã được tạo
-- SELECT id, order_code, created FROM orders ORDER BY id DESC FETCH FIRST 1 ROWS ONLY;

-- =====================================================
-- ROLLBACK SCRIPT (NẾU CẦN)
-- =====================================================
/*
-- Xóa các bảng và cột đã tạo (chỉ dùng khi cần rollback)

DROP TABLE order_notes CASCADE CONSTRAINTS;
DROP TABLE order_status_history CASCADE CONSTRAINTS;

ALTER TABLE orders DROP COLUMN order_code;
ALTER TABLE orders DROP COLUMN total_amount;
ALTER TABLE orders DROP COLUMN shipping_fee;
ALTER TABLE orders DROP COLUMN shipping_address;
ALTER TABLE orders DROP COLUMN receiver_name;
ALTER TABLE orders DROP COLUMN receiver_phone;
ALTER TABLE orders DROP COLUMN payment_method;
ALTER TABLE orders DROP COLUMN is_paid;
ALTER TABLE orders DROP COLUMN paid_at;
ALTER TABLE orders DROP COLUMN confirmed_at;
ALTER TABLE orders DROP COLUMN shipped_at;
ALTER TABLE orders DROP COLUMN delivered_at;
ALTER TABLE orders DROP COLUMN cancelled_at;
ALTER TABLE orders DROP COLUMN cancel_reason;

DROP TRIGGER trg_orders_code;
*/

-- =====================================================
-- KẾT THÚC SCRIPT
-- =====================================================

SELECT 'Script đã chạy thành công!' as message FROM dual;

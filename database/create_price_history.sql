-- =====================================================
-- SCRIPT: Tạo bảng lịch sử thay đổi giá sản phẩm
-- Database: Oracle
-- Mục đích: Lưu lại mọi thay đổi giá để theo dõi và phân tích
-- =====================================================

-- 1. Tạo bảng PRICE_HISTORY
CREATE TABLE price_history (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id NUMBER NOT NULL,
    old_price NUMBER(15,2),
    new_price NUMBER(15,2) NOT NULL,
    change_percentage NUMBER(5,2),
    changed_by VARCHAR2(100) NOT NULL,
    change_reason VARCHAR2(500),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_price_history_product 
        FOREIGN KEY (product_id) 
        REFERENCES products(id) 
        ON DELETE CASCADE
);

-- 2. Tạo index để tăng tốc query
CREATE INDEX idx_price_history_product ON price_history(product_id);
CREATE INDEX idx_price_history_date ON price_history(changed_at);

-- 3. Thêm comment cho bảng và cột
COMMENT ON TABLE price_history IS 'Lịch sử thay đổi giá sản phẩm';
COMMENT ON COLUMN price_history.id IS 'ID tự động tăng';
COMMENT ON COLUMN price_history.product_id IS 'ID sản phẩm (FK)';
COMMENT ON COLUMN price_history.old_price IS 'Giá cũ (NULL nếu là lần đầu set giá)';
COMMENT ON COLUMN price_history.new_price IS 'Giá mới';
COMMENT ON COLUMN price_history.change_percentage IS 'Phần trăm thay đổi (+ tăng, - giảm)';
COMMENT ON COLUMN price_history.changed_by IS 'Người thay đổi (username)';
COMMENT ON COLUMN price_history.change_reason IS 'Lý do thay đổi giá';
COMMENT ON COLUMN price_history.changed_at IS 'Thời gian thay đổi';

-- 4. Tạo trigger tự động tính change_percentage
CREATE OR REPLACE TRIGGER trg_price_history_percentage
BEFORE INSERT ON price_history
FOR EACH ROW
BEGIN
    -- Tính phần trăm thay đổi nếu có old_price
    IF :NEW.old_price IS NOT NULL AND :NEW.old_price > 0 THEN
        :NEW.change_percentage := ROUND(((:NEW.new_price - :NEW.old_price) / :NEW.old_price) * 100, 2);
    ELSE
        :NEW.change_percentage := NULL;
    END IF;
END;
/

-- 5. Insert dữ liệu mẫu (tùy chọn - để test)
-- Giả sử product_id = 1 là Dell XPS 15
INSERT INTO price_history (product_id, old_price, new_price, changed_by, change_reason)
VALUES (1, NULL, 25000000, 'admin', 'Giá khởi điểm khi thêm sản phẩm');

INSERT INTO price_history (product_id, old_price, new_price, changed_by, change_reason)
VALUES (1, 25000000, 23000000, 'admin', 'Khuyến mãi cuối năm - giảm 8%');

INSERT INTO price_history (product_id, old_price, new_price, changed_by, change_reason)
VALUES (1, 23000000, 24000000, 'admin', 'Điều chỉnh giá sau khuyến mãi');

-- 6. Verify
SELECT 
    ph.id,
    p.name AS product_name,
    ph.old_price,
    ph.new_price,
    ph.change_percentage || '%' AS change_pct,
    ph.changed_by,
    ph.change_reason,
    TO_CHAR(ph.changed_at, 'DD/MM/YYYY HH24:MI:SS') AS changed_at
FROM price_history ph
JOIN products p ON ph.product_id = p.id
ORDER BY ph.changed_at DESC;

-- =====================================================
-- KẾT QUẢ MONG ĐỢI:
-- - Bảng price_history được tạo thành công
-- - Trigger tự động tính change_percentage
-- - Index để tăng tốc query
-- - Dữ liệu mẫu (nếu có sản phẩm id=1)
-- =====================================================

COMMIT;

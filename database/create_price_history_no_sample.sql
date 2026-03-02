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

-- 5. Verify
SELECT 
    table_name, 
    column_name, 
    data_type, 
    data_length
FROM user_tab_columns 
WHERE table_name = 'PRICE_HISTORY'
ORDER BY column_id;

-- Kiểm tra trigger
SELECT trigger_name, status 
FROM user_triggers 
WHERE trigger_name = 'TRG_PRICE_HISTORY_PERCENTAGE';

-- =====================================================
-- KẾT QUẢ MONG ĐỢI:
-- - Bảng price_history được tạo thành công
-- - Trigger tự động tính change_percentage
-- - Index để tăng tốc query
-- =====================================================

COMMIT;

-- =====================================================
-- SCRIPT SỬA LỖI VÀ TIẾP TỤC
-- Chạy script này nếu gặp lỗi ORA-01408
-- =====================================================

-- Bỏ qua lỗi index đã tồn tại, tiếp tục tạo các index khác
BEGIN
    EXECUTE IMMEDIATE 'CREATE INDEX idx_orders_payment ON orders(payment_method)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1408 THEN
            DBMS_OUTPUT.PUT_LINE('Index đã tồn tại, bỏ qua...');
        ELSE
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'CREATE INDEX idx_orders_paid ON orders(is_paid)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1408 THEN
            DBMS_OUTPUT.PUT_LINE('Index đã tồn tại, bỏ qua...');
        ELSE
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'CREATE INDEX idx_orders_receiver_phone ON orders(receiver_phone)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1408 THEN
            DBMS_OUTPUT.PUT_LINE('Index đã tồn tại, bỏ qua...');
        ELSE
            RAISE;
        END IF;
END;
/

-- Kiểm tra kết quả
SELECT 'Đã tạo xong các indexes!' as status FROM dual;

-- Tiếp tục với phần còn lại...

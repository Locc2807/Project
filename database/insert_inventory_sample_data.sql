-- Script thêm dữ liệu mẫu cho Inventory
-- Chạy script này SAU KHI đã có sản phẩm trong bảng products

-- Bước 1: Kiểm tra các sản phẩm hiện có
SELECT id, name, category_id FROM products ORDER BY id;

-- Bước 2: Thêm tồn kho cho các sản phẩm
-- Thay đổi product_id theo ID thực tế trong hệ thống của bạn

-- Ví dụ: Nếu bạn có sản phẩm với ID từ 1-10, uncomment và chạy:
/*
-- Thêm tồn kho cho sản phẩm ID 1
INSERT INTO inventory (product_id, quantity, min_stock_level) VALUES (1, 50, 10);

-- Thêm tồn kho cho sản phẩm ID 2
INSERT INTO inventory (product_id, quantity, min_stock_level) VALUES (2, 30, 10);

-- Thêm tồn kho cho sản phẩm ID 3 (sắp hết hàng)
INSERT INTO inventory (product_id, quantity, min_stock_level) VALUES (3, 8, 10);

-- Thêm tồn kho cho sản phẩm ID 4
INSERT INTO inventory (product_id, quantity, min_stock_level) VALUES (4, 100, 20);

-- Thêm tồn kho cho sản phẩm ID 5 (sắp hết hàng)
INSERT INTO inventory (product_id, quantity, min_stock_level) VALUES (5, 5, 10);

-- Thêm giao dịch nhập kho cho sản phẩm 1
INSERT INTO inventory_transactions (product_id, transaction_type, quantity, note, created_by) 
VALUES (1, 'IMPORT', 50, 'Nhập kho đầu tiên', 'admin');

-- Thêm giao dịch nhập kho cho sản phẩm 2
INSERT INTO inventory_transactions (product_id, transaction_type, quantity, note, created_by) 
VALUES (2, 'IMPORT', 30, 'Nhập kho đầu tiên', 'admin');

-- Thêm giao dịch cho sản phẩm 3
INSERT INTO inventory_transactions (product_id, transaction_type, quantity, note, created_by) 
VALUES (3, 'IMPORT', 10, 'Nhập kho đầu tiên', 'admin');

INSERT INTO inventory_transactions (product_id, transaction_type, quantity, note, created_by) 
VALUES (3, 'EXPORT', 2, 'Xuất kho bán hàng', 'admin');

COMMIT;
*/

-- HOẶC: Tự động thêm tồn kho cho TẤT CẢ sản phẩm hiện có
-- Uncomment và chạy đoạn này để tự động tạo inventory cho tất cả products:

/*
-- Thêm inventory cho tất cả sản phẩm chưa có
INSERT INTO inventory (product_id, quantity, min_stock_level)
SELECT p.id, 0, 10
FROM products p
WHERE NOT EXISTS (
    SELECT 1 FROM inventory i WHERE i.product_id = p.id
);

-- Thêm giao dịch khởi tạo
INSERT INTO inventory_transactions (product_id, transaction_type, quantity, note, created_by)
SELECT product_id, 'ADJUSTMENT', 0, 'Khởi tạo tồn kho', 'system'
FROM inventory
WHERE product_id NOT IN (
    SELECT DISTINCT product_id FROM inventory_transactions
);

COMMIT;

-- Kiểm tra kết quả
SELECT 
    i.id,
    p.name as product_name,
    i.quantity,
    i.min_stock_level,
    i.last_updated
FROM inventory i
JOIN products p ON i.product_id = p.id
ORDER BY i.id;
*/

package com.bkap.services;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.bkap.entity.OrderDetail;
import com.bkap.entity.Orders;

/**
 * Service để tạo hóa đơn PDF
 * Lưu ý: Đây là implementation đơn giản dùng text-based PDF
 * Để có PDF đẹp hơn, nên dùng thư viện như iText hoặc Apache PDFBox
 */
@Service
public class PdfInvoiceService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    /**
     * Tạo hóa đơn PDF cho đơn hàng
     * 
     * @param order Đơn hàng cần tạo hóa đơn
     * @return byte array của file PDF
     */
    public byte[] generateInvoicePDF(Orders order) {
        // Tạo nội dung hóa đơn dạng text
        StringBuilder content = new StringBuilder();
        
        content.append("=".repeat(60)).append("\n");
        content.append("                    HÓA ĐƠN BÁN HÀNG\n");
        content.append("=".repeat(60)).append("\n\n");
        
        // Thông tin đơn hàng
        content.append("Mã đơn hàng: #").append(order.getId()).append("\n");
        content.append("Ngày đặt: ").append(order.getCreated().format(DATE_FORMATTER)).append("\n");
        content.append("Trạng thái: ").append(getStatusName(order.getStatus())).append("\n\n");
        
        // Thông tin khách hàng
        content.append("-".repeat(60)).append("\n");
        content.append("THÔNG TIN KHÁCH HÀNG\n");
        content.append("-".repeat(60)).append("\n");
        content.append("Tên: ").append(order.getCustomer().getName()).append("\n");
        content.append("Email: ").append(order.getCustomer().getEmail()).append("\n");
        content.append("Số điện thoại: ").append(order.getCustomer().getPhone() != null ? 
                                                  order.getCustomer().getPhone() : "N/A").append("\n");
        content.append("Địa chỉ: ").append(order.getCustomer().getAddress() != null ? 
                                            order.getCustomer().getAddress() : "N/A").append("\n\n");
        
        // Chi tiết sản phẩm
        content.append("-".repeat(60)).append("\n");
        content.append("CHI TIẾT ĐƠN HÀNG\n");
        content.append("-".repeat(60)).append("\n");
        content.append(String.format("%-30s %10s %15s %15s\n", 
                                     "Sản phẩm", "SL", "Đơn giá", "Thành tiền"));
        content.append("-".repeat(60)).append("\n");
        
        double totalAmount = 0;
        for (OrderDetail detail : order.getOrderDetails()) {
            String productName = detail.getProduct().getName();
            if (productName.length() > 28) {
                productName = productName.substring(0, 25) + "...";
            }
            
            int quantity = detail.getQuantity();
            double price = detail.getPrice();
            double subtotal = quantity * price;
            totalAmount += subtotal;
            
            content.append(String.format("%-30s %10d %15s %15s\n",
                                        productName,
                                        quantity,
                                        CURRENCY_FORMATTER.format(price),
                                        CURRENCY_FORMATTER.format(subtotal)));
        }
        
        content.append("-".repeat(60)).append("\n");
        content.append(String.format("%-30s %10s %15s %15s\n",
                                     "",
                                     "",
                                     "TỔNG CỘNG:",
                                     CURRENCY_FORMATTER.format(totalAmount)));
        content.append("=".repeat(60)).append("\n\n");
        
        // Ghi chú
        if (order.getOrder_note() != null && !order.getOrder_note().isEmpty()) {
            content.append("Ghi chú: ").append(order.getOrder_note()).append("\n\n");
        }
        
        content.append("Cảm ơn quý khách đã mua hàng!\n");
        content.append("Hotline: 1900-xxxx | Email: support@ecommerce.com\n");
        
        // Convert sang byte array (đơn giản hóa, trong thực tế nên dùng thư viện PDF)
        return content.toString().getBytes();
    }
    
    /**
     * Tạo phiếu giao hàng
     */
    public byte[] generateDeliveryNotePDF(Orders order) {
        StringBuilder content = new StringBuilder();
        
        content.append("=".repeat(60)).append("\n");
        content.append("                  PHIẾU GIAO HÀNG\n");
        content.append("=".repeat(60)).append("\n\n");
        
        content.append("Mã đơn hàng: #").append(order.getId()).append("\n");
        content.append("Ngày giao: ").append(order.getCreated().format(DATE_FORMATTER)).append("\n\n");
        
        // Thông tin người nhận
        content.append("NGƯỜI NHẬN:\n");
        content.append("Tên: ").append(order.getCustomer().getName()).append("\n");
        content.append("SĐT: ").append(order.getCustomer().getPhone() != null ? 
                                       order.getCustomer().getPhone() : "N/A").append("\n");
        content.append("Địa chỉ: ").append(order.getCustomer().getAddress() != null ? 
                                           order.getCustomer().getAddress() : "N/A").append("\n\n");
        
        // Danh sách sản phẩm
        content.append("DANH SÁCH SẢN PHẨM:\n");
        content.append("-".repeat(60)).append("\n");
        
        int index = 1;
        for (OrderDetail detail : order.getOrderDetails()) {
            content.append(index++).append(". ")
                   .append(detail.getProduct().getName())
                   .append(" - SL: ").append(detail.getQuantity())
                   .append("\n");
        }
        
        content.append("\n");
        content.append("Chữ ký người giao: _______________  Chữ ký người nhận: _______________\n");
        
        return content.toString().getBytes();
    }
    
    private String getStatusName(int status) {
        return switch (status) {
            case 1 -> "Chờ xác nhận";
            case 2 -> "Đã xác nhận";
            case 3 -> "Đang chuyển";
            case 4 -> "Đang giao";
            case 5 -> "Đã hủy";
            case 6 -> "Thành công";
            case 7 -> "Yêu cầu hủy";
            default -> "Không xác định";
        };
    }
}

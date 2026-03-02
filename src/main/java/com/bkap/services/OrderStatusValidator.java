package com.bkap.services;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service để validate việc chuyển trạng thái đơn hàng
 * Đảm bảo logic nghiệp vụ đúng, tránh chuyển trạng thái không hợp lệ
 */
@Component
public class OrderStatusValidator {

    // Định nghĩa các trạng thái
    public static final int STATUS_PENDING = 1;        // Chờ xác nhận
    public static final int STATUS_CONFIRMED = 2;      // Đã xác nhận
    public static final int STATUS_PROCESSING = 3;     // Đang chuyển
    public static final int STATUS_SHIPPING = 4;       // Đang giao
    public static final int STATUS_CANCELLED = 5;      // Đã hủy
    public static final int STATUS_COMPLETED = 6;      // Thành công
    public static final int STATUS_CANCEL_REQUEST = 7; // Yêu cầu hủy

    // Map các trạng thái có thể chuyển đến
    private static final Map<Integer, List<Integer>> VALID_TRANSITIONS = new HashMap<>();

    static {
        // Từ Chờ xác nhận (1) → có thể chuyển sang
        VALID_TRANSITIONS.put(STATUS_PENDING, List.of(
            STATUS_CONFIRMED,      // Xác nhận
            STATUS_CANCELLED       // Hủy
        ));

        // Từ Đã xác nhận (2) → có thể chuyển sang
        VALID_TRANSITIONS.put(STATUS_CONFIRMED, List.of(
            STATUS_PROCESSING,     // Đang chuyển
            STATUS_CANCELLED       // Hủy (nếu cần)
        ));

        // Từ Đang chuyển (3) → có thể chuyển sang
        VALID_TRANSITIONS.put(STATUS_PROCESSING, List.of(
            STATUS_SHIPPING,       // Đang giao
            STATUS_CANCELLED       // Hủy (trường hợp đặc biệt)
        ));

        // Từ Đang giao (4) → có thể chuyển sang
        VALID_TRANSITIONS.put(STATUS_SHIPPING, List.of(
            STATUS_COMPLETED,      // Thành công
            STATUS_CANCEL_REQUEST  // Yêu cầu hủy (khách từ chối nhận)
        ));

        // Từ Đã hủy (5) → không thể chuyển sang trạng thái nào
        VALID_TRANSITIONS.put(STATUS_CANCELLED, List.of());

        // Từ Thành công (6) → không thể chuyển sang trạng thái nào
        VALID_TRANSITIONS.put(STATUS_COMPLETED, List.of());

        // Từ Yêu cầu hủy (7) → có thể chuyển sang
        VALID_TRANSITIONS.put(STATUS_CANCEL_REQUEST, List.of(
            STATUS_CANCELLED,      // Chấp nhận hủy
            STATUS_COMPLETED       // Từ chối hủy, giao thành công
        ));
    }

    /**
     * Kiểm tra xem có thể chuyển từ oldStatus sang newStatus không
     */
    public boolean isValidTransition(int oldStatus, int newStatus) {
        // Nếu giữ nguyên trạng thái thì OK
        if (oldStatus == newStatus) {
            return true;
        }

        List<Integer> validNextStatuses = VALID_TRANSITIONS.get(oldStatus);
        if (validNextStatuses == null) {
            return false;
        }

        return validNextStatuses.contains(newStatus);
    }

    /**
     * Lấy danh sách trạng thái có thể chuyển đến
     */
    public List<Integer> getValidNextStatuses(int currentStatus) {
        return VALID_TRANSITIONS.getOrDefault(currentStatus, List.of());
    }

    /**
     * Lấy tên trạng thái
     */
    public String getStatusName(int status) {
        return switch (status) {
            case STATUS_PENDING -> "Chờ xác nhận";
            case STATUS_CONFIRMED -> "Đã xác nhận";
            case STATUS_PROCESSING -> "Đang chuyển";
            case STATUS_SHIPPING -> "Đang giao";
            case STATUS_CANCELLED -> "Đã hủy";
            case STATUS_COMPLETED -> "Thành công";
            case STATUS_CANCEL_REQUEST -> "Yêu cầu hủy";
            default -> "Không xác định";
        };
    }

    /**
     * Lấy thông báo lỗi khi chuyển trạng thái không hợp lệ
     */
    public String getTransitionErrorMessage(int oldStatus, int newStatus) {
        return String.format(
            "Không thể chuyển từ trạng thái '%s' sang '%s'",
            getStatusName(oldStatus),
            getStatusName(newStatus)
        );
    }
}

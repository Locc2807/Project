package com.bkap.exception;

/**
 * Exception khi chuyển trạng thái đơn hàng không hợp lệ
 */
public class InvalidOrderStatusTransitionException extends RuntimeException {
    
    private final int oldStatus;
    private final int newStatus;

    public InvalidOrderStatusTransitionException(String message, int oldStatus, int newStatus) {
        super(message);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public int getOldStatus() {
        return oldStatus;
    }

    public int getNewStatus() {
        return newStatus;
    }
}

package com.bkap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bkap.services.OrderService;

@ControllerAdvice
public class GlobalModelAttribute {

	@Autowired
    private OrderService orderService;

    @ModelAttribute("pendingCount")
    public long getPendingOrderCount() {
        return orderService.countByStatus(1); // 1 = Chờ xác nhận
    }
}

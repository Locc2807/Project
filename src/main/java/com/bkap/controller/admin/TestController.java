package com.bkap.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class TestController {

    @GetMapping("/test-cdn")
    public String testCdn() {
        return "admin/test-cdn";
    }
}

package com.bkap.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.entity.Inventory;
import com.bkap.repository.InventoryRepository;

@RestController
@RequestMapping("/admin/inventory-test")
public class InventoryTestController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping("/count")
    public String testCount() {
        long count = inventoryRepository.count();
        return "Total inventory records: " + count;
    }

    @GetMapping("/list")
    public List<Inventory> testList() {
        return inventoryRepository.findAll();
    }
}

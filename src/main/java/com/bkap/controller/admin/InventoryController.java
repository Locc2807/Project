package com.bkap.controller.admin;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bkap.entity.Inventory;
import com.bkap.entity.InventoryTransaction;
import com.bkap.entity.Product;
import com.bkap.services.InventoryService;
import com.bkap.services.ProductService;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String index(Model model, @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(required = false) String keyword) {
        Page<Inventory> page;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = inventoryService.searchByProductName(keyword, pageNo);
            model.addAttribute("keyword", keyword);
        } else {
            page = inventoryService.getAll(pageNo);
        }

        model.addAttribute("inventories", page.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("lowStockCount", inventoryService.countLowStockProducts());

        return "admin/inventory/index";
    }

    @GetMapping("/low-stock")
    public String lowStock(Model model, @RequestParam(defaultValue = "1") Integer pageNo) {
        Page<Inventory> page = inventoryService.findLowStockProducts(pageNo);

        model.addAttribute("inventories", page.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        return "admin/inventory/low-stock";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("inventory", new Inventory());
        model.addAttribute("products", productService.getAll());
        return "admin/inventory/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Inventory inventory, RedirectAttributes redirectAttributes) {
        if (inventoryService.create(inventory)) {
            redirectAttributes.addFlashAttribute("success", "Thêm tồn kho thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Thêm tồn kho thất bại!");
        }
        return "redirect:/admin/inventory";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Inventory> inventory = inventoryService.findById(id);
        if (inventory.isPresent()) {
            model.addAttribute("inventory", inventory.get());
            model.addAttribute("products", productService.getAll());
            return "admin/inventory/edit";
        }
        return "redirect:/admin/inventory";
    }

    @PostMapping("/edit")
    public String edit(@ModelAttribute Inventory inventory, RedirectAttributes redirectAttributes) {
        if (inventoryService.update(inventory)) {
            redirectAttributes.addFlashAttribute("success", "Cập nhật tồn kho thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Cập nhật tồn kho thất bại!");
        }
        return "redirect:/admin/inventory";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (inventoryService.delete(id)) {
            redirectAttributes.addFlashAttribute("success", "Xóa tồn kho thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Xóa tồn kho thất bại!");
        }
        return "redirect:/admin/inventory";
    }

    @GetMapping("/adjust/{productId}")
    public String showAdjustForm(@PathVariable Long productId, Model model) {
        Optional<Product> product = productService.findById(productId);
        Optional<Inventory> inventory = inventoryService.findByProductId(productId);

        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            model.addAttribute("inventory", inventory.orElse(null));
            return "admin/inventory/adjust";
        }
        return "redirect:/admin/inventory";
    }

    @PostMapping("/adjust")
    public String adjust(@RequestParam Long productId, @RequestParam Integer quantity,
            @RequestParam String transactionType, @RequestParam(required = false) String note,
            Principal principal, RedirectAttributes redirectAttributes) {

        String username = principal != null ? principal.getName() : "admin";

        if (inventoryService.adjustStock(productId, quantity, transactionType, note, username)) {
            redirectAttributes.addFlashAttribute("success", "Điều chỉnh kho thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Điều chỉnh kho thất bại!");
        }
        return "redirect:/admin/inventory";
    }

    @GetMapping("/transactions")
    public String transactions(Model model, @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(required = false) String keyword) {
        Page<InventoryTransaction> page;

        if (keyword != null && !keyword.trim().isEmpty()) {
            page = inventoryService.searchTransactionsByProductName(keyword, pageNo);
            model.addAttribute("keyword", keyword);
        } else {
            page = inventoryService.getAllTransactions(pageNo);
        }

        model.addAttribute("transactions", page.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        return "admin/inventory/transactions";
    }

    @GetMapping("/transactions/{productId}")
    public String productTransactions(@PathVariable Long productId, Model model) {
        Optional<Product> product = productService.findById(productId);
        if (product.isPresent()) {
            List<InventoryTransaction> transactions = inventoryService.getTransactionsByProductId(productId);
            model.addAttribute("product", product.get());
            model.addAttribute("transactions", transactions);
            return "admin/inventory/product-transactions";
        }
        return "redirect:/admin/inventory";
    }
}

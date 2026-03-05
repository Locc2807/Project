package com.bkap.controller;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bkap.api.Cart;
import com.bkap.entity.Customer;
import com.bkap.entity.OrderDetail;
import com.bkap.entity.Orders;
import com.bkap.entity.Product;
import com.bkap.entity.User;
import com.bkap.model.CartItem;
import com.bkap.services.CustomerService;
import com.bkap.services.InventoryService;
import com.bkap.services.OrderService;
import com.bkap.services.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    // Trang checkout
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes redirect) {
        System.out.println("=== CHECKOUT DEBUG ===");
        System.out.println("Session ID: " + session.getId());
        
        Cart cart = (Cart) session.getAttribute("cart");
        System.out.println("Cart from session: " + cart);
        
        if (cart == null || cart.isEmpty()) {
            System.out.println("Cart is null or empty - redirecting to /cart");
            redirect.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }
        
        System.out.println("Cart has " + cart.getTotalItems() + " items");

        // Lấy thông tin user nếu đã đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            System.out.println("User logged in: " + username);
            Customer customer = customerService.findByEmail(username);
            if (customer != null) {
                model.addAttribute("customer", customer);
            }
        } else {
            System.out.println("User not logged in");
        }

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getCartItems());
        model.addAttribute("totalPrice", cart.getTotalPrice());
        model.addAttribute("shippingFee", 0.0); // Miễn phí ship
        model.addAttribute("finalTotal", cart.getTotalPrice());
        
        System.out.println("Returning checkout view");
        System.out.println("=====================");

        return "checkout";
    }

    // Xử lý đặt hàng
    @PostMapping("/checkout/place-order")
    public String placeOrder(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("receiverEmail") String receiverEmail,
            @RequestParam("shippingAddress") String shippingAddress,
            @RequestParam(value = "orderNote", required = false) String orderNote,
            @RequestParam("paymentMethod") String paymentMethod,
            HttpSession session,
            RedirectAttributes redirect) {

        try {
            Cart cart = (Cart) session.getAttribute("cart");
            
            if (cart == null || cart.isEmpty()) {
                redirect.addFlashAttribute("error", "Giỏ hàng trống");
                return "redirect:/cart";
            }

            // 1. Tạo hoặc lấy Customer
            Customer customer = customerService.findByEmail(receiverEmail);
            if (customer == null) {
                customer = new Customer();
                customer.setName(receiverName);
                customer.setEmail(receiverEmail);
                customer.setPhone(receiverPhone);
                customer.setAddress(shippingAddress);
                customer.setCreated(new Date());
                customerService.create(customer);
            }

            // 2. Tạo Order
            Orders order = new Orders();
            order.setCustomer(customer);
            order.setReceiverName(receiverName);
            order.setReceiverPhone(receiverPhone);
            order.setShippingAddress(shippingAddress);
            order.setOrder_note(orderNote);
            order.setPaymentMethod(paymentMethod);
            order.setTotalAmount(cart.getTotalPrice());
            order.setShippingFee(0.0);
            order.setStatus(1); // 1 = Chờ xác nhận
            order.setIsPaid(false);
            order.setCreated(LocalDateTime.now());

            // Lưu order (orderCode sẽ được tạo tự động bởi trigger)
            orderService.create(order);

            // 3. Tạo OrderDetails và trừ tồn kho
            for (CartItem item : cart.getCartItems()) {
                Product product = productService.findById(item.getId()).orElse(null);
                if (product == null) continue;

                // Tạo OrderDetail
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProduct(product);
                detail.setQuantity(item.getQuantity());
                detail.setPrice(item.getPrice().doubleValue());
                orderService.createOrderDetail(detail);

                // Trừ tồn kho
                inventoryService.adjustStock(
                    product.getId(),
                    -item.getQuantity(), // Số âm = xuất kho
                    "OUT",
                    "Bán hàng - Đơn #" + order.getId(),
                    "system"
                );
            }

            // 4. Xóa giỏ hàng
            cart.clear();
            session.setAttribute("cart", cart);

            // 5. Redirect đến trang thành công
            redirect.addFlashAttribute("success", "Đặt hàng thành công!");
            redirect.addFlashAttribute("orderId", order.getId());
            redirect.addFlashAttribute("orderCode", order.getOrderCode());
            
            return "redirect:/checkout/success";

        } catch (Exception e) {
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    // Trang đặt hàng thành công
    @GetMapping("/checkout/success")
    public String checkoutSuccess(Model model) {
        return "checkout-success";
    }
}

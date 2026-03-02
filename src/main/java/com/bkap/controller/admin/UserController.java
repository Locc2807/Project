package com.bkap.controller.admin;

import com.bkap.entity.User;
import com.bkap.services.EmailService;
import com.bkap.services.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
	@Autowired
	private EmailService emailService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@GetMapping("/logon")
	public String loginPage(@RequestParam(value = "error", required = false) String error, HttpServletRequest request,
			Model model) {
		String rememberedUsername = null;
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("rememberUsername".equals(cookie.getName())) {
					rememberedUsername = cookie.getValue();
					break;
				}
			}
		}
		model.addAttribute("rememberedUsername", rememberedUsername);

		if (error != null) {
			model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
		}
		return "/admin/logon";
	}

	@PostMapping("/logon")
	public String login(@RequestParam String username, @RequestParam String password,
			@RequestParam(required = false) String remember, HttpServletResponse response, HttpSession session,
			Model model) {

		User user = userService.findByUserName(username);

		if (user == null) {
			model.addAttribute("error", "Tên đăng nhập không tồn tại");
			return "/admin/logon";
		}

		// Nếu mật khẩu lưu dạng mã hóa BCrypt
		if (!passwordEncoder.matches(password, user.getPassword())) {
			model.addAttribute("error", "Mật khẩu không đúng");
			return "/admin/logon";
		}

		// Đăng nhập thành công
		session.setAttribute("user", user);

		if ("on".equals(remember)) {
			Cookie cookie = new Cookie("rememberUsername", username);
			cookie.setMaxAge(7 * 24 * 60 * 60);
			cookie.setPath("/");
			response.addCookie(cookie);
		} else {
			Cookie cookie = new Cookie("rememberUsername", null);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
		}

		return "redirect:/admin";
	}

	@GetMapping("/signup")
	public String showRegisterForm() {
		return "admin/signup";
	}

	@PostMapping("/signup")
	public String processRegister(@RequestParam String username, @RequestParam String email,
			@RequestParam String password, @RequestParam String confirmPassword, Model model) {
		if (!password.equals(confirmPassword)) {
			model.addAttribute("error", "Passwords do not match");
			return "admin/signup";
		}
		if (userService.existsByUserName(username)) {
			model.addAttribute("error", "Username already exists");
			return "admin/signup";
		}
		if (userService.existsByEmail(email)) {
			model.addAttribute("error", "Email already registered");
			return "admin/signup";
		}

		String encodedPassword = passwordEncoder.encode(password);

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(encodedPassword);
		user.setRole("ROLE_USER");
		user.setEnabled(true);

		userService.save(user);

		return "redirect:/logon?signupSuccess=true";
	}

	@GetMapping("/forgot-password")
	public String showForgotPasswordForm() {
		return "admin/forgot-password";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam("email") String email, Model model) {
		User user = userService.findByEmail(email);
		if (user == null) {
			model.addAttribute("message", "Email không tồn tại.");
			return "admin/forgot-password";
		}
		// Gửi link dạng: http://localhost:8080/reset-password?email=xxx
		String resetLink = "http://localhost:8080/reset-password?email=" + email;
		emailService.sendEmail(email, "Reset Password Link", "Click link để đổi mật khẩu: " + resetLink);
		model.addAttribute("message", "Đã gửi email. Vui lòng kiểm tra hộp thư.");
		return "admin/forgot-password";
	}

	@GetMapping("/reset-password")
	public String showResetPasswordForm(@RequestParam("email") String email, Model model) {
		model.addAttribute("email", email);
		return "admin/reset-password";
	}

	@PostMapping("/reset-password")
	public String processResetPassword(@RequestParam("email") String email, @RequestParam("password") String password,
			Model model) {
		User user = userService.findByEmail(email);
		if (user == null) {
			model.addAttribute("message", "Email không hợp lệ.");
			return "admin/reset-password";
		}
		user.setPassword(passwordEncoder.encode(password));
		userService.save(user);
		model.addAttribute("message", "Đổi mật khẩu thành công.");
		return "admin/logon";
	}

}

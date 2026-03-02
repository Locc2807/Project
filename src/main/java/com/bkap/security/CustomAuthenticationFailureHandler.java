package com.bkap.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		String errorMessage = "Đăng nhập thất bại. Vui lòng thử lại.";

		if (exception instanceof DisabledException) {
			errorMessage = "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.";
		} else if (exception instanceof BadCredentialsException) {
			errorMessage = "Sai tên đăng nhập hoặc mật khẩu.";
		}

		request.getSession().setAttribute("loginError", true);
		request.getSession().setAttribute("errorMessage", errorMessage);
		response.sendRedirect("/user/login?error=true");
	}
}

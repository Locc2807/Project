package com.bkap.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;

import com.bkap.security.CustomAuthenticationFailureHandler;
import com.bkap.security.CustomUserDetailService;

@Configuration
public class SecurityContextConfig {
	@Autowired
	private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
	
	@Autowired
	@Qualifier("adminSecurityContextRepository")
	private SecurityContextRepository adminRepo;

	@Autowired
	@Qualifier("userSecurityContextRepository")
	private SecurityContextRepository userRepo;

	@Autowired
	private CustomUserDetailService customUserDetailService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Bean
	@Primary
	public DaoAuthenticationProvider authenticationProvider() {
	    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	    authProvider.setUserDetailsService(customUserDetailService);
	    authProvider.setPasswordEncoder(passwordEncoder);
	    return authProvider;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
	    http.securityMatcher("/admin/**", "/logon", "/signup", "/forgot-password", "/reset-password", "/admin-logout")
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/logon", "/signup", "/forgot-password", "/reset-password").permitAll()
	            .anyRequest().authenticated())
	        .formLogin(form -> form
	            .loginPage("/logon")
	            .loginProcessingUrl("/logon")
	            .usernameParameter("username")
	            .passwordParameter("password")
	            .defaultSuccessUrl("/admin", true)
	            .permitAll())
	        .logout(logout -> logout.logoutUrl("/admin-logout").logoutSuccessUrl("/logon"))
	        .csrf(csrf -> csrf.disable())
	        .securityContext(security -> security.securityContextRepository(adminRepo)) // 🟢 Thêm dòng này
	        .authenticationProvider(authenticationProvider());

	    return http.build();
	}


	@Bean
	@Order(2)
	public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
	    http.authorizeHttpRequests(auth -> auth
	            .requestMatchers(
	                "/", "/deals", "/categories/**", "/laptops/**", "/product/quickview/**", "/smartphones/**", 
	                "/cameras/**", "/accessories/**",
	                "/user/login", "/user/register", "/user/forgot_password"
	            ).permitAll()
	            .requestMatchers("/user/profile", "/orders/**").authenticated()
	            .anyRequest().permitAll() // hoặc .authenticated() nếu bạn muốn bảo vệ các URL khác
	        )
	        .formLogin(form -> form
	            .loginPage("/user/login")
	            .loginProcessingUrl("/user/login")
	            .usernameParameter("username")
	            .passwordParameter("password")
	            .defaultSuccessUrl("/?loginSuccess=true", true)
	            .failureHandler(customAuthenticationFailureHandler)
	            .permitAll()
	        )
	        .logout(logout -> logout
	            .logoutUrl("/user/logout")
	            .logoutSuccessUrl("/user/login")
	        )
	        .csrf(csrf -> csrf.disable())
	        .securityContext(security -> security.securityContextRepository(userRepo))
	        .authenticationProvider(authenticationProvider());

	    return http.build();
	}


}

/*
 * package com.bkap.service;
 * 
 * import com.bkap.dto.DashboardStatsDTO; import
 * com.bkap.repository.OrderRepository; import
 * com.bkap.repository.CustomerRepository; import
 * com.bkap.repository.VisitorRepository;
 * 
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.stereotype.Service;
 * 
 * import java.time.Month; import java.util.stream.IntStream;
 * 
 * @Service public class DashboardService {
 * 
 * @Autowired private OrderRepository orderRepository;
 * 
 * @Autowired private CustomerRepository customerRepository;
 * 
 * @Autowired private VisitorRepository visitorRepository;
 * 
 * public DashboardStatsDTO getDashboardStats() { int[] registrations = new
 * int[12]; int[] orders = new int[12]; int[] visitors = new int[12];
 * 
 * for (int i = 1; i <= 12; i++) { registrations[i - 1] =
 * customerRepository.countByMonth(i); orders[i - 1] =
 * orderRepository.countByMonth(i); visitors[i - 1] =
 * visitorRepository.countByMonth(i); }
 * 
 * return new DashboardStatsDTO(registrations, orders, visitors); } }
 */
package com.bkap.services;

import com.bkap.entity.Customer;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface CustomerService {
    List<Customer> getNewestCustomers();
    List<Customer> getAll();
    Optional<Customer> findById(Long id);
    Page<Customer> findAll(int page, int size);
    Page<Customer> searchByKeyword(String keyword, int page, int size);
    Customer findByEmail(String email);
    Boolean save(Customer customer);
    Boolean create(Customer customer);
}

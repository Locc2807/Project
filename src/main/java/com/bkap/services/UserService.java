package com.bkap.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bkap.entity.User;

public interface UserService {
    User findByUserName(String username);
    User findByEmail(String email);
    Boolean create(User user);
    User findById(Long id);
    List<User> getAll();
    Boolean delete(Long id);
    Boolean existsByUserName(String username);
    Boolean existsByEmail(String email);
    User save(User user);

    // Dashboard
    long countAllUsers();
}

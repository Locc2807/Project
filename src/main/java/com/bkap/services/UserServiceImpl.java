package com.bkap.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.stereotype.Service;

import com.bkap.entity.User;
import com.bkap.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public User findByUserName(String username) {
		// TODO Auto-generated method stub
		return userRepository.findByUsername(username).orElse(null);
	}

	@Override
	public Boolean create(User user) {
		try {
			this.userRepository.save(user);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public User findById(Long id) {
		// TODO Auto-generated method stub
		return this.userRepository.findById(id).get();
	}

	@Override
	public List<User> getAll() {
		// TODO Auto-generated method stub
		return this.userRepository.findAll();
	}

	@Override
	public Boolean delete(Long id) {
		try {
			this.userRepository.delete(findById(id));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public User findByEmail(String email) {
		// TODO Auto-generated method stub
		return userRepository.findByEmail(email).orElse(null);
	}

	@Override
	public Boolean existsByUserName(String username) {
		// TODO Auto-generated method stub
		return userRepository.existsByUsername(username);
	}

	@Override
	public Boolean existsByEmail(String email) {
		// TODO Auto-generated method stub
		return userRepository.existsByEmail(email);
	}

	@Override
	public User save(User user) {
		// TODO Auto-generated method stub
		return userRepository.save(user);
	}

	@Override
	public long countAllUsers() {
		// TODO Auto-generated method stub
		return userRepository.count();
	}

}

package com.bkap.services;

import com.bkap.entity.Customer;
import com.bkap.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerRepository;

	@Override
	public List<Customer> getNewestCustomers() {
		return customerRepository.findTop5ByOrderByCreatedDesc();
	}

	@Override
	public List<Customer> getAll() {
		// TODO Auto-generated method stub
		return customerRepository.findAll();
	}

	@Override
	public Optional<Customer> findById(Long id) {
		// TODO Auto-generated method stub
		return customerRepository.findById(id);
	}

	@Override
	public Page<Customer> findAll(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return customerRepository.findAll(pageable);
	}

	@Override
	public Page<Customer> searchByKeyword(String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return customerRepository.searchByNameEmailOrPhone(keyword, pageable);
	}

	@Override
	public Customer findByEmail(String email) {
		return customerRepository.findByEmail(email);
	}

	@Override
	public Boolean save(Customer customer) {
		try {
			customerRepository.save(customer);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public Boolean create(Customer customer) {
		try {
			customerRepository.save(customer);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

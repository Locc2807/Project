package com.bkap.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bkap.entity.HotDeal;

public interface HotDealService {

	Page<HotDeal> findAll(Pageable pageable);

	HotDeal save(HotDeal hotDeal);

	Boolean delete(Long id);

	Optional<HotDeal> findById(Long id);

	Boolean update(HotDeal hotDeal);

	Page<HotDeal> findAllPageable(Pageable pageable);

	Page<HotDeal> filter(String keyword, Integer status, LocalDate start, LocalDate end, Pageable pageable);

	List<HotDeal> getActiveDeals();
}

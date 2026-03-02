package com.bkap.services;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bkap.entity.HotDeal;
import com.bkap.repository.HotDealRepository;

@Service
public class HotDealServiceImpl implements HotDealService {
	@Autowired
	private HotDealRepository hotDealRepository;

	@Override
	public Page<HotDeal> findAll(Pageable pageable) {
		return hotDealRepository.findAll(pageable);
	}

	@Override
	public HotDeal save(HotDeal hotDeal) {
		// TODO Auto-generated method stub
		return hotDealRepository.save(hotDeal);

	}

	@Override
	public Boolean delete(Long id) {
		try {
			hotDealRepository.deleteById(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Optional<HotDeal> findById(Long id) {
		// TODO Auto-generated method stub
		return hotDealRepository.findById(id);
	}

	@Override
	public Boolean update(HotDeal hotDeal) {
		try {
			hotDealRepository.save(hotDeal);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Page<HotDeal> findAllPageable(Pageable pageable) {
		return hotDealRepository.findAll(pageable);
	}

	@Override
	public Page<HotDeal> filter(String keyword, Integer status, LocalDate start, LocalDate end, Pageable pageable) {
		// TODO Auto-generated method stub
		return hotDealRepository.filterAll(keyword, status, start, end, pageable);
	}

	@Override
	public List<HotDeal> getActiveDeals() {
		// TODO Auto-generated method stub
		return hotDealRepository.findByEndDateAfter(LocalDate.now());
	}

	
}

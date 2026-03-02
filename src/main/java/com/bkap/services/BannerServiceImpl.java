package com.bkap.services;


import com.bkap.entity.Banner;
import com.bkap.repository.BannerRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl implements BannerService{
	
	@Autowired
	private BannerRepository bannerRepository;
    
	@Override
	public List<Banner> getAll() {
		// TODO Auto-generated method stub
		return this.bannerRepository.findAll();
	}

	@Override
	public Boolean create(Banner banner) {
		try {
			this.bannerRepository.save(banner);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}		
		return false;
	}

	@Override
	public Banner findById(Integer id) {
		// TODO Auto-generated method stub
		return this.bannerRepository.findById(id).get();
	}

	@Override
	public Boolean update(Banner banner) {
		try {
			this.bannerRepository.save(banner);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}		
		return false;
	}

	@Override
	public Boolean delete(Integer id) {
		try {
			this.bannerRepository.delete(findById(id));
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}		
		return false;
	}

	@Override
	public List<Banner> searchBanner(String keyword) {
		// TODO Auto-generated method stub
		return this.bannerRepository.searchBanner(keyword);
	}

	@Override
	public Page<Banner> getAll(Integer pageNo) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(pageNo - 1, 2);
		return this.bannerRepository.findAll(pageable);
	}

	@Override
	public Page<Banner> searchBanner(String keywrod, Integer pageNo) {
		List list = this.searchBanner(keywrod);
		
		Pageable pageable = PageRequest.of(pageNo - 1, 2);
		
		Integer start = (int) pageable.getOffset();
		Integer end = (int) ((pageable.getOffset() + pageable.getPageSize()) > list.size() ? list.size() : pageable.getOffset() + pageable.getPageSize());
		
		list = list.subList(start, end);
		
		return new PageImpl<Banner>(list, pageable, this.searchBanner(keywrod).size());
	}

   

}

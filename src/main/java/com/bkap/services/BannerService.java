package com.bkap.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.bkap.entity.Banner;

public interface BannerService {
	List<Banner> getAll();
	Boolean create(Banner banner);
	Banner findById(Integer id);
	Boolean update(Banner banner);
	Boolean delete(Integer id);
	List<Banner> searchBanner(String keyword);
	
	Page<Banner> getAll(Integer pageNo);
	Page<Banner> searchBanner(String keywrod, Integer pageNo);
}

package com.bkap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkap.entity.Banner;

public interface BannerRepository extends JpaRepository<Banner, Integer> {

	@Query("SELECT c FROM Banner c WHERE c.title LIKE CONCAT('%', :keyword, '%')")
	List<Banner> searchBanner(@Param("keyword") String keyword);

}

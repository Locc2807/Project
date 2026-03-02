package com.bkap.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bkap.entity.HotDeal;

public interface HotDealRepository extends JpaRepository<HotDeal, Long> {
	List<HotDeal> findByEndDateAfter(LocalDate today);

	@Query("SELECT h FROM HotDeal h WHERE "
			+ "(:keyword IS NULL OR LOWER(h.product.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
			+ "(:status IS NULL OR h.status = :status) AND " + "(:start IS NULL OR h.startDate >= :start) AND "
			+ "(:end IS NULL OR h.endDate <= :end)")
	Page<HotDeal> filterAll(@Param("keyword") String keyword, @Param("status") Integer status,
			@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);

}

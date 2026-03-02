package com.bkap.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bkap.dto.HotDealViewModel;
import com.bkap.entity.HotDeal;
import com.bkap.services.HotDealService;

@Controller
public class DealsController {

	@Autowired
	private HotDealService hotDealService;

	@GetMapping("/deals")
	public String showHotDeals(Model model) {
		List<HotDeal> hotDeals = hotDealService.getActiveDeals();

		// Chuyển sang ViewModel để định dạng sẵn giá, ngày
		List<HotDealViewModel> dealVMs = hotDeals.stream().map(HotDealViewModel::new).collect(Collectors.toList());

		model.addAttribute("dealVMs", dealVMs);
		model.addAttribute("breadcrumbTitle", "Hot Deals");
		return "deals";
	}
}

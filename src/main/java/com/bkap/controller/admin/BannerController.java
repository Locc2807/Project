package com.bkap.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bkap.entity.Banner;
import com.bkap.services.BannerService;
import com.bkap.services.StorageService;

@Controller
public class BannerController {
	@Autowired
	private StorageService storageService;
	
	@Autowired
	private BannerService bannerService;
	
	@GetMapping("/admin/banner")
	public String index(Model model, @Param("keyword") String keyword,
			@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo) {
		Page<Banner> listBanner = this.bannerService.getAll(pageNo);
		
		if(keyword != null) {
			listBanner = this.bannerService.searchBanner(keyword, pageNo);
			model.addAttribute("keyword", keyword);
		}
		
		model.addAttribute("totalPage", listBanner.getTotalPages());
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("listBanner", listBanner);
		return "admin/banner/index";
	}
	
	@GetMapping("/admin/add-banner")
	public String add(Model model) {
		Banner banner = new Banner();
		model.addAttribute("banner", banner);
		banner.setStatus(true);
		
		return "admin/banner/add";
	}
	
	@PostMapping("/admin/add-banner")
	public String save(@ModelAttribute("banner") Banner banner, @RequestParam("imageFile") MultipartFile file,
			RedirectAttributes redirect) {
		//Upload file
		String fileName = this.storageService.store(file);
		banner.setImage(fileName);
		
		if(bannerService.create(banner)) {
			redirect.addFlashAttribute("success", "Thêm banner thành công");
			return "redirect:/admin/banner";
		} else {
			redirect.addFlashAttribute("error", "Thêm thất bại");
			return "redirect:/admin/add-banner";
		}
	}
	
	@GetMapping("/admin/edit-banner/{id}")
	public String edit(Model model, @PathVariable("id") Integer id) {
		Banner banner = this.bannerService.findById(id);
		model.addAttribute("banner", banner);
		
		return "admin/banner/edit";
	}
	
	@PostMapping("/admin/edit-banner")
	public String update (@ModelAttribute("banner") Banner banner, @RequestParam("imageFile") MultipartFile file,
			RedirectAttributes redirect) {
		if(!file.isEmpty()) {
			String filename = storageService.store(file);
			banner.setImage(filename);
		}
		
		if(bannerService.update(banner)) {
			redirect.addFlashAttribute("success", "Cập nhật banner thành công");
			return "redirect:/admin/banner"; 
		} else {
			redirect.addFlashAttribute("error", "Cập nhật thất bại");
			return "redirect:/admin/edit-banner/" + banner.getId();
		}
	}
	
	@GetMapping("/admin/delete-banner/{id}")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes redirect) {
		if(bannerService.delete(id)) {
			redirect.addFlashAttribute("success", "Xóa banner thành công");
		} else {
			redirect.addFlashAttribute("error", "Xóa banner thất bại");
		}	
		return "redirect:/admin/banner";
	}
}
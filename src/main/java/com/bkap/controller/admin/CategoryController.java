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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bkap.entity.Category;
import com.bkap.services.CategoryService;
import com.bkap.services.StorageService;

@Controller
@RequestMapping("/admin")
public class CategoryController {
	@Autowired
	private StorageService storageService;

	@Autowired
	private CategoryService categoryService;

	@GetMapping("/category")
	public String index(Model model, @Param("keyword") String keyword,
			@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo) {
		Page<Category> list = this.categoryService.getAll(pageNo);

		if (keyword != null) {
			list = this.categoryService.searchCategory(keyword, pageNo);
			model.addAttribute("keyword", keyword);
		}

		model.addAttribute("totalPage", list.getTotalPages());
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("list", list);
		return "admin/category/index";
	}

	@GetMapping("/add-category")
	public String add(Model model) {
		Category category = new Category();
		category.setStatus(true);
		model.addAttribute("category", category);
		return "admin/category/add";
	}

	@PostMapping("/add-category")
	public String save(@ModelAttribute("category") Category category, @RequestParam("imageFile") MultipartFile file,
			RedirectAttributes redirect) {

		String fileName = this.storageService.store(file); 
		category.setImage(fileName); 

		if (categoryService.create(category)) {
			redirect.addFlashAttribute("success", "Thêm danh mục thành công");
			return "redirect:/admin/category";
		} else {
			redirect.addFlashAttribute("error", "Thêm thất bại");
			return "redirect:/admin/add-category";
		}
	}

	@GetMapping("/edit-category/{id}")
	public String edit(Model model, @PathVariable("id") Integer id) {
		Category category = this.categoryService.findById(id);
		model.addAttribute("category", category);
		return "admin/category/edit";
	}

	@PostMapping("/edit-category")
	public String update(@ModelAttribute("category") Category category, @RequestParam("imageFile") MultipartFile file,
			RedirectAttributes redirect) {

		if (!file.isEmpty()) {
			String filename = storageService.store(file);
			category.setImage(filename);
		}

		if (categoryService.update(category)) {
			redirect.addFlashAttribute("success", "Cập nhật danh mục thành công");
			return "redirect:/admin/category";
		} else {
			redirect.addFlashAttribute("error", "Cập nhật thất bại");
			return "redirect:/admin/edit-category/" + category.getId();
		}
	}

	@GetMapping("/delete-category/{id}")
	public String delete(@PathVariable("id") Integer id, RedirectAttributes redirect) {
		if (categoryService.delete(id)) {
			redirect.addFlashAttribute("success", "Xóa danh mục thành công");
		} else {
			redirect.addFlashAttribute("error", "Xóa danh mục thất bại");
		}
		return "redirect:/admin/category";
	}

}

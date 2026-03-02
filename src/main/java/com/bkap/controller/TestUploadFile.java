package com.bkap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.bkap.services.StorageService;

@Controller
public class TestUploadFile {
	@Autowired
	private StorageService storageService;
	@GetMapping("/upload-test")
	
	public String uploadDemo() {
		
		return "test-upload";
	}
	
	@PostMapping("/upload-test")
	public String save(@RequestParam("file") MultipartFile file) {
		
		this.storageService.store(file);
		return "test-upload";
	}
}

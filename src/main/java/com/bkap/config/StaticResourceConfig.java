package com.bkap.config;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Uploads folder (external)
		String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(uploadPath, "classpath:/static/uploads/")
				.setCachePeriod(0);

		// Frontend assets
		registry.addResourceHandler("/fe/**")
				.addResourceLocations("classpath:/static/fe/")
				.setCachePeriod(0);

		// Images
		registry.addResourceHandler("/images/**")
				.addResourceLocations("classpath:/static/images/")
				.setCachePeriod(0);

		// Assets
		registry.addResourceHandler("/assets/**")
				.addResourceLocations("classpath:/static/assets/")
				.setCachePeriod(0);
	}
}

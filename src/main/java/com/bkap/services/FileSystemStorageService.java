package com.bkap.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

	private final Path storageFolder = Paths.get("uploads");

	public FileSystemStorageService() {
		try {
			Files.createDirectories(storageFolder);
		} catch (IOException e) {
			throw new RuntimeException("Không thể tạo thư mục lưu trữ", e);
		}
	}

	@Override
	public String store(MultipartFile file) {
		String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
		try(InputStream input = file.getInputStream()) {
			Files.copy(input, storageFolder.resolve(filename),
					StandardCopyOption.REPLACE_EXISTING);
			return filename;
		} catch (Exception e) {
			throw new RuntimeException("Lỗi khi lưu file.", e);
		}
	}

}

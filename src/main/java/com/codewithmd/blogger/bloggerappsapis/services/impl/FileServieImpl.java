package com.codewithmd.blogger.bloggerappsapis.services.impl;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.codewithmd.blogger.bloggerappsapis.services.interfaces.FileService;

@Service
public class FileServieImpl implements FileService {

	@Override
	public String uploadImage(String path, MultipartFile file, Integer postId, String imageName) throws IOException {
		// File name
		String name = file.getOriginalFilename();
		String randomId;
		if (imageName != null) {
			randomId = imageName;
		} else {
			randomId = UUID.randomUUID().toString();
		}

		String fileName1 = randomId.concat(name.substring(name.lastIndexOf(".")));
		String filePath = path + File.separator + fileName1;
		// createFolder if not created

		File f = new File(path);

		if (!f.exists()) {
			f.mkdir();
		}
		// file copy
		Files.copy(file.getInputStream(), Paths.get(filePath));

		return fileName1;
	}

	@Override
	public InputStream getResource(String path, String fileName) throws FileNotFoundException {
		String fullPath = path + File.separator + fileName;
		return new FileInputStream(fullPath);

	}
}

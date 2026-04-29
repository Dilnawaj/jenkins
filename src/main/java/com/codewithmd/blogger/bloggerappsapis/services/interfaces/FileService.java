package com.codewithmd.blogger.bloggerappsapis.services.interfaces;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	
	String uploadImage(String path, MultipartFile file,Integer postId,String imageName) throws IOException;
	
	InputStream getResource(String path, String fileName) throws FileNotFoundException;


}

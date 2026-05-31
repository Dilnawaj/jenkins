package com.codewithmd.blogger.bloggerappsapis.services.interfaces;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;

import com.codewithmd.blogger.bloggerappsapis.payloads.PostDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	
	String uploadImage(String path, MultipartFile file, Integer postId, String imageName, PostDto postDto) throws IOException;


}

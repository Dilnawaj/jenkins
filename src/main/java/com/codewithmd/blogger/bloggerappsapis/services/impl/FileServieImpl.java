package com.codewithmd.blogger.bloggerappsapis.services.impl;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.codewithmd.blogger.bloggerappsapis.services.interfaces.FileService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;

@Service
public class FileServieImpl implements FileService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;
	@Override
    public String uploadImage(String path, MultipartFile file,
                              Integer postId, String imageName) throws IOException {
        String name = file.getOriginalFilename();
        if (name == null || name.equals("")) {
            return "default.PNG";
        }

        String randomId = (imageName != null) ? imageName : UUID.randomUUID().toString();
        String fileName = randomId.concat(name.substring(name.lastIndexOf(".")));
        String s3Key = "blog-images/" + fileName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        // Return full S3 public URL
        return "https://" + bucketName + ".s3.amazonaws.com/" + s3Key;
    }


}

package com.codewithmd.blogger.bloggerappsapis.services.impl;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.codewithmd.blogger.bloggerappsapis.config.SqsService;
import com.codewithmd.blogger.bloggerappsapis.config.ThirdPartyApi;
import com.codewithmd.blogger.bloggerappsapis.payloads.BlogAI;
import com.codewithmd.blogger.bloggerappsapis.payloads.FileData;
import com.codewithmd.blogger.bloggerappsapis.payloads.PostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.codewithmd.blogger.bloggerappsapis.services.interfaces.FileService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;

@Service
public class FileServiceImpl implements FileService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private SqsService sqsService;  // ✅ ADD THIS

    @Autowired
    private ThirdPartyApi thirdPartyApi;


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String uploadImage(String path, MultipartFile file,
                              Integer id, String imageName, PostDto postDto) throws IOException {

        if (file == null) {
            return thirdPartyApi.getImageBySpringAI(postDto.getTitle(),postDto.getContent());
        }
        String name = file.getOriginalFilename();
        String randomId = (imageName != null) ? imageName : UUID.randomUUID().toString();
        String fileName = randomId.concat(name.substring(name.lastIndexOf(".")));
        String s3Key = "blog-images/" + fileName;  // path ignored, using S3 key

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
        logger.error("Error uploading files to S3", s3Key);
        // Returns full S3 URL instead of just filename
        return "https://" + bucketName + ".s3.amazonaws.com/" + s3Key;
    }
    // ── Private: upload blogs to S3 ──
    public  List<String> uploadToS3(List<FileData> blogAis, Integer userId, Integer jobId) {
        List<String> uploadedUrls = new ArrayList<>();

        try {
            for (FileData blogAI : blogAis) {

                // Safe file name
                String fileName = blogAI.getFileName();
                String s3Key = "uploads/user" + userId + "/job" + jobId + "/" + fileName;

                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType("application/octet-stream")
                        .build();


                s3Client.putObject(putRequest, RequestBody.fromString(blogAI.getFileData()));

// ✅ ADD THIS LINE
                sqsService.sendS3ReferenceToSqs(bucketName, s3Key,userId, jobId);

                uploadedUrls.add("https://" + bucketName + ".s3.amazonaws.com/" + s3Key);
            }

            return uploadedUrls;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error uploading files to S3", e);
            //throw new RuntimeException("Failed to upload files to S3", e);
        }
        return new ArrayList<>();
    }


}

package com.codewithmd.blogger.bloggerappsapis.services.impl;

import com.codewithmd.blogger.bloggerappsapis.config.FileParserUtil;
import com.codewithmd.blogger.bloggerappsapis.payloads.BlogAI;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class SqsMessageListener {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final PostServiceImpl postServiceImpl;
    private final Logger logger = LoggerFactory.getLogger(SqsMessageListener.class);

    @PostConstruct
    public void init() {
        logger.info("✅ SqsMessageListener bean initialized and ready");
    }

    @SqsListener(value = "${aws.sqs.queue-name}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onMessage(String rawMessage) {
        try {
            logger.info("🔥 onMessage triggered → {}", rawMessage);

            Map<String, Object> payload = objectMapper.readValue(rawMessage, Map.class);
            String bucket  = (String) payload.get("bucket");
            String key     = (String) payload.get("key");
            Integer userId = Integer.parseInt((String) payload.get("userId"));
            Integer jobId  = Integer.parseInt((String) payload.get("jobId"));

            byte[] fileBytes = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(key).build()
            ).readAllBytes();

            String fileName = key.substring(key.lastIndexOf("/") + 1);
            byte[] decoded  = Base64.getDecoder().decode(fileBytes);
            BlogAI blog     = FileParserUtil.parseSingleFileFromBytes(fileName, decoded);
            blog.setFileName(fileName);  // ✅ set fileName so addSinglePost can match it

            postServiceImpl.addSinglePost(blog, userId, jobId);

        } catch (Exception e) {
            logger.error("❌ Failed to process SQS message: {}", rawMessage, e);

        }
    }
}
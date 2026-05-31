package com.codewithmd.blogger.bloggerappsapis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

@Service
public class SqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${aws.sqs.queue-url}")
    private String sqsQueueUrl;

    public SqsService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void sendS3ReferenceToSqs(String bucket, String key, Integer userId, Integer jobId) {
        try {
            Map<String, String> payload = Map.of(
                    "bucket", bucket,
                    "key", key,
                    "source", "blogger-app",
                    "userId", userId.toString(),
                    "jobId",  jobId.toString()
            );

            String messageBody = objectMapper.writeValueAsString(payload);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(sqsQueueUrl)
                    .messageBody(messageBody)
                    .build());

            logger.info("✅ Sent to SQS → bucket: {}, key: {}", bucket, key);

        } catch (Exception e) {
            logger.error("❌ Failed to send message to SQS", e);
            e.printStackTrace();
        }
    }

}
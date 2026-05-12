package com.codewithmd.blogger.bloggerappsapis.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BlogMetricsService {

    private final MeterRegistry meterRegistry;

    // Count post creations
    public void recordPostCreated() {
        meterRegistry.counter("blogger.posts.created").increment();
    }



    // Track image upload time
    public void recordUploadTime(long milliseconds) {
        meterRegistry.timer("blogger.image.upload.time")
                .record(milliseconds, TimeUnit.MILLISECONDS);
    }



    public void recordPostDeleted() {
        meterRegistry.counter("blogger.posts.deleted").increment();
    }

    // Users
    public void recordUserRegistered() {
        meterRegistry.counter("blogger.users.registered").increment();
    }

    public void recordUserLogin() {
        meterRegistry.counter("blogger.users.login").increment();
    }

    public void recordLoginFailed() {
        meterRegistry.counter("blogger.users.login.failed").increment();
    }



    // Email
    public void recordEmailSent() {
        meterRegistry.counter("blogger.email.sent").increment();
    }

    public void recordEmailFailed() {
        meterRegistry.counter("blogger.email.failed").increment();
    }
}
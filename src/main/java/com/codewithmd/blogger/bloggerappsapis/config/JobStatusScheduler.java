package com.codewithmd.blogger.bloggerappsapis.config;

import com.codewithmd.blogger.bloggerappsapis.entities.UploadFile;
import com.codewithmd.blogger.bloggerappsapis.payloads.FilesUploadTrack;
import com.codewithmd.blogger.bloggerappsapis.repos.UploadFileRepo;
import com.codewithmd.blogger.bloggerappsapis.services.impl.PostServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JobStatusScheduler {

    @Autowired
    PostServiceImpl postService;

    private final UploadFileRepo uploadFileRepo;
    private final Logger logger = LoggerFactory.getLogger(JobStatusScheduler.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // ✅ Triggers ONCE after 5 seconds delay (gives time for transaction to commit)
    public void scheduleFinalize(Integer jobId,Integer userId) {
        scheduler.schedule(() -> finalizeJobStatus(jobId,userId), 5, TimeUnit.SECONDS);
    }

    @Transactional
    public void finalizeJobStatus(Integer jobId,Integer userId
    ) {
        try {
            UploadFile job = uploadFileRepo.findById(jobId).get();

            int total   = job.getTotal_files();
            int success = job.getProcessed_files();
            int failed  = job.getFailed_files();

            logger.info("🔍 Job {} → total:{} success:{} failed:{}", jobId, total, success, failed);

            if (success + failed >= total) {
                FilesUploadTrack finalStatus = failed > 0
                        ? FilesUploadTrack.PARTIAL_FAILED
                        : FilesUploadTrack.COMPLETED;

                uploadFileRepo.updateStatus(jobId, finalStatus);

                postService.pushBulkStatus(jobId, userId);
                logger.info("🏁 Job {} finalized → {}", jobId, finalStatus);

            } else {
                // ✅ Not done yet — retry once more after 5 seconds
                logger.info("⏳ Job {} not done yet, retrying in 5s...", jobId);
                scheduler.schedule(() -> finalizeJobStatus(jobId,userId), 5, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            logger.error("❌ Failed to finalize job {}", jobId, e);
        }
    }
}
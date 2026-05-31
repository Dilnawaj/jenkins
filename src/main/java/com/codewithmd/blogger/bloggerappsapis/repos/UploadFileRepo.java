package com.codewithmd.blogger.bloggerappsapis.repos;

import com.codewithmd.blogger.bloggerappsapis.entities.UploadFile;
import com.codewithmd.blogger.bloggerappsapis.payloads.BulkStatus;
import com.codewithmd.blogger.bloggerappsapis.payloads.FilesUploadTrack;
import com.codewithmd.blogger.bloggerappsapis.payloads.WelcomeEmailModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UploadFileRepo extends JpaRepository<UploadFile, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE UploadFile u SET u.processed_files = u.processed_files + 1 WHERE u.file_id = :jobId")
    void incrementSuccess(@Param("jobId") Integer jobId);

    @Modifying
    @Transactional
    @Query("UPDATE UploadFile u SET u.failed_files = u.failed_files + 1 WHERE u.file_id = :jobId")
    void incrementFailed(@Param("jobId") Integer jobId);

    @Query("SELECT new com.codewithmd.blogger.bloggerappsapis.payloads.BulkStatus(u.status, u.total_files,u.processed_files, u.failed_files) FROM UploadFile u  WHERE u.file_id= ?1 and u.user_id = ?2")
    public BulkStatus getUploadStatus(Integer fileId, Integer userId);


    @Modifying
    @Transactional
    @Query("UPDATE UploadFile u SET u.status = :status WHERE u.file_id = :jobId")
    void updateStatus(@Param("jobId") Integer jobId, @Param("status") FilesUploadTrack status);

}

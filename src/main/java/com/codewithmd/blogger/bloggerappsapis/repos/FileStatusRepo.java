package com.codewithmd.blogger.bloggerappsapis.repos;

import com.codewithmd.blogger.bloggerappsapis.entities.FileStatus;
import com.codewithmd.blogger.bloggerappsapis.payloads.BulkStatus;
import com.codewithmd.blogger.bloggerappsapis.payloads.FileUploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileStatusRepo extends JpaRepository<FileStatus, Integer> {

    List<FileStatus> findByFileId(Integer fileId);

    Optional<FileStatus> findByFileIdAndFileName(Integer fileId, String fileName);

    @Query("SELECT new com.codewithmd.blogger.bloggerappsapis.payloads.FileUploadStatus(u.fileName, u.status,u.error_message) FROM FileStatus u  WHERE u.fileId= ?1 ")
    public List<FileUploadStatus> getFileStatus(Integer fileId);
}

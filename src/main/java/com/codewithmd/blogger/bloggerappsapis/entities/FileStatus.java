package com.codewithmd.blogger.bloggerappsapis.entities;

import com.codewithmd.blogger.bloggerappsapis.payloads.FileTrack;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class FileStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer fileId;
    private String fileName;
    @Enumerated(EnumType.STRING)
    private FileTrack status;
    private String error_message;
    private Integer blogId;
    private Date processedAt;

    public FileStatus() {
    }

    public FileStatus(Integer fileId, FileTrack status,String fileName, String error_message, Integer blogId, Date processedAt) {

        this.fileId = fileId;
        this.fileName = fileName;
        this.status = status;
        this.error_message = error_message;
        this.blogId = blogId;
        this.processedAt = processedAt;
    }
}

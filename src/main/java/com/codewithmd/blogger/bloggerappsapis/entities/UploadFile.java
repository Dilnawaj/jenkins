package com.codewithmd.blogger.bloggerappsapis.entities;

import com.codewithmd.blogger.bloggerappsapis.payloads.FileTrack;
import com.codewithmd.blogger.bloggerappsapis.payloads.FilesUploadTrack;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class UploadFile {
    @Id
    private Integer file_id;

    private Integer user_id;
    private Integer total_files;
    private Integer processed_files;
    private Integer failed_files;
    @Enumerated(EnumType.STRING)
    private FilesUploadTrack status;
    private Date created_at;

    public UploadFile() {

    }

    public UploadFile(Integer file_id, Integer user_id, Integer total_files, Integer processed_files, FilesUploadTrack status, Date created_at, Integer failed_files) {
        this.file_id = file_id;
        this.user_id = user_id;
        this.total_files = total_files;
        this.processed_files = processed_files;
        this.status = status;
        this.created_at = created_at;
        this.failed_files = failed_files;
    }

}

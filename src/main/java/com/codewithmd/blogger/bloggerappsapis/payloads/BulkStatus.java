package com.codewithmd.blogger.bloggerappsapis.payloads;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BulkStatus {

    private FilesUploadTrack status;

    private Integer  totalFiles;
    private Integer   processedFiles;
    private Integer failedFiles;

    private List<FileUploadStatus> fileUploadStatus = new ArrayList<>();


    public BulkStatus() {
    }

    public BulkStatus(FilesUploadTrack status, Integer totalFiles, Integer processedFiles, Integer failedFiles) {
        this.status = status;
        this.totalFiles = totalFiles;
        this.processedFiles = processedFiles;
        this.failedFiles = failedFiles;
    }




}

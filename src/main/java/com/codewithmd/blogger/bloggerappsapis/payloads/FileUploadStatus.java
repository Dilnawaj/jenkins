package com.codewithmd.blogger.bloggerappsapis.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;


@Data
public class FileUploadStatus {



    private String fileName;

    @Enumerated(EnumType.STRING)
    private FileTrack status;
    @JsonProperty
    private String error_message;

    public FileUploadStatus() {
    }

    public FileUploadStatus(String fileName, FileTrack status, String error_message) {
        this.fileName = fileName;
        this.status = status;
        this.error_message = error_message;
    }
}

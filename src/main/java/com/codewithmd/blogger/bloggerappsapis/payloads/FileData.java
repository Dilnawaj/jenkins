package com.codewithmd.blogger.bloggerappsapis.payloads;

import lombok.Data;

@Data
public class FileData {

    private String fileName;

    private String fileData;


    public FileData() {
    }

    public FileData(String fileName, String fileData) {
        this.fileData = fileData;
        this.fileName = fileName;
    }
}

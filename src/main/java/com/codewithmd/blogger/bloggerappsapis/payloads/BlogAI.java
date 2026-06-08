package com.codewithmd.blogger.bloggerappsapis.payloads;

import com.codewithmd.blogger.bloggerappsapis.entities.FileStatus;
import lombok.Data;

@Data
public class BlogAI {


    private String fileName;

    private String title;

    private String content;

    private Integer userId;

    private Integer jobId;

    private FileStatus fileStatus;
}

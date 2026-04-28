package com.jenkins;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class JenkinsTest {

    @GetMapping
    public ResponseEntity<String> testJenkins()
    {
        return ResponseEntity.ok("Java running successfully.");
    }
    @GetMapping("jenkins")
    public ResponseEntity<String> testJenkinsApi()
    {
        return ResponseEntity.ok("Jenkins running successfully.");
    }
    @GetMapping("jemkins")
    public ResponseEntity<String> testJemkinsApi()
    {
        return ResponseEntity.ok("Jemkins running successfully.");
    }


}

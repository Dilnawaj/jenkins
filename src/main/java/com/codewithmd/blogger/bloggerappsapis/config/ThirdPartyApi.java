package com.codewithmd.blogger.bloggerappsapis.config;

import com.codewithmd.blogger.bloggerappsapis.payloads.BlogAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;        // ← correct one
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ThirdPartyApi {
    @Value("${third.party.api.ai}")
    private String thirdPartyAIURl;

    public String getImageBySpringAI(String postTitle, String postContent) {

        System.out.println("Post Title "+ postTitle);

        System.out.println("Post Content "+ postContent);


        RestTemplate restTemplate = new RestTemplate();
        String aiServiceUrl = thirdPartyAIURl+"image"; // ← also make sure port is 8082 not 8081

        BlogAI blogAI = new BlogAI();
        blogAI.setTitle(postTitle);
        blogAI.setContent(postContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BlogAI> request = new HttpEntity<>(blogAI, headers);

        return restTemplate.postForObject(aiServiceUrl, request, String.class);
    }


    public Map<String, String> getCategoryAndImageBySpringAI(String postTitle, String postContent) {

        System.out.println("Post Title "+ postTitle);

        System.out.println("Post Content "+ postContent);


        RestTemplate restTemplate = new RestTemplate();
        String aiServiceUrl = thirdPartyAIURl+"/image/category"; // ← also make sure port is 8082 not 8081

        BlogAI blogAI = new BlogAI();
        blogAI.setTitle(postTitle);
        blogAI.setContent(postContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BlogAI> request = new HttpEntity<>(blogAI, headers);

        String data =  restTemplate.postForObject(aiServiceUrl, request, String.class);
        Map<String, String> response = restTemplate.postForObject(
                aiServiceUrl,
                request,
                Map.class
        );
return response;

    }
}
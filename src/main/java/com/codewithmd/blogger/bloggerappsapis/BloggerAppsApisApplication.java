package com.codewithmd.blogger.bloggerappsapis;


import org.springframework.boot.SpringApplication;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
@EnableAsync
@EnableScheduling
@EnableSwagger2WebMvc
@SpringBootApplication
public class BloggerAppsApisApplication {

	public static void main(String[] args) {

		SpringApplication.run(BloggerAppsApisApplication.class, args);

	}

}

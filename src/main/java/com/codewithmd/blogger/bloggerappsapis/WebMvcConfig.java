package com.codewithmd.blogger.bloggerappsapis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.codewithmd.blogger.bloggerappsapis.account.service.CustomRequestInterceptor;


@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

	@Autowired
	private CustomRequestInterceptor customRequestInterceptor;
	
	@Value("${allowedOrigins}")
	private String allowedOrigins;
	
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(customRequestInterceptor);
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedMethods(RequestMethod.GET.toString(), RequestMethod.POST.toString(),
						RequestMethod.PUT.toString(), RequestMethod.PATCH.toString(), RequestMethod.DELETE.toString())
				.allowedOrigins(allowedOrigins.split(","));
	}
}
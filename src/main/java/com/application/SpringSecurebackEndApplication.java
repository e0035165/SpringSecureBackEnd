package com.application;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@ComponentScan(basePackages= {"com.service","com.filter","com.application","com.repositories","com.controllerz","com.entity","com.repositories"})
@EntityScan(basePackages= {"com.entity"})
@EnableJpaRepositories(basePackages= {"com.repositories"})
public class SpringSecurebackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurebackEndApplication.class, args);
	}

	
	@Bean
	public RestTemplate getRestTemplateBuilder(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofMillis(5000))
				.setReadTimeout(Duration.ofMillis(5000))
				.build();
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}

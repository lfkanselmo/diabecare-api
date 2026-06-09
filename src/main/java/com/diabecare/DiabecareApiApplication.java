package com.diabecare;

import com.diabecare.infrastructure.config.DiabeCareProperties;
import com.diabecare.infrastructure.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({DiabeCareProperties.class,JwtProperties.class})
public class DiabecareApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiabecareApiApplication.class, args);
	}

}

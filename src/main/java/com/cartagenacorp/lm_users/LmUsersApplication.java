package com.cartagenacorp.lm_users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class LmUsersApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(LmUsersApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(LmUsersApplication.class);
	}

}

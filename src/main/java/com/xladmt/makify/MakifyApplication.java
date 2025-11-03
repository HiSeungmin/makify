package com.xladmt.makify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MakifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MakifyApplication.class, args);
	}

}

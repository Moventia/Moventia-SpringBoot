package com.Moventia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoventiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoventiaApplication.class, args);
	}

}

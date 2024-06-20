package com.quotes_museum.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.NoSuchAlgorithmException;

@SpringBootApplication
public class QuotesMuseumBackendApplication {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		SpringApplication.run(QuotesMuseumBackendApplication.class, args);
	}
}

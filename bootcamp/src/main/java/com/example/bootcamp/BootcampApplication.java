package com.example.bootcamp;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BootcampApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootcampApplication.class, args);
	}
	
	@Component("uuid")
	/*public static */class UuidService {
		public String buildUuid() {
			return UUID.randomUUID().toString();
		}
	}
	
	@Bean
	Bar bar(Foo foo, @Value("#{ uuid.buildUuid()}")String uuid,
			@Value("#{ 2 > 1}")boolean proceed) {
		return new Bar(foo, uuid, proceed );
	}
	
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}

@RestController
class IsbnRestController {
	
	private final RestTemplate restTemplate;
	public IsbnRestController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	// http://localhost:8080/books/1449374646
	@GetMapping("/books/{isbn}")
	String lookUpBookByIsbn(@PathVariable("isbn") String isbn) {
		ResponseEntity<String> exchange = this.restTemplate
				.exchange("https://www.googleapis.com/books/v1/volumes?q=isbn:"+isbn,
				HttpMethod.GET, null, String.class);
		String body = exchange.getBody();
		return body;
	}
}
	
	

//@Component
class Bar {
	private final Foo foo;
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	Bar(Foo foo, /*@Value("#{ uuid.buildUuid()}")*/String uuid,
			/*@Value("#{ 2 > 1}") */ boolean proceed) {
		this.foo = foo;
		this.log.info("UUID: "+uuid);
		this.log.info("proceed: "+proceed);
	}
}

@Component
class Foo {
	
}

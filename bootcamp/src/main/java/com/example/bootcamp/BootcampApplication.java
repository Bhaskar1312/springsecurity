package com.example.bootcamp;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
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

@Component
class LoggingFilter implements javax.servlet.Filter {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		Assert.isTrue(request instanceof HttpServletRequest, "this assumes you have a HTTP request");
//		Assert.isInstanceOf(HttpServletRequest.class, request, "this assumes you have a HTTP request");
		
		HttpServletRequest httpServletRequest= HttpServletRequest.class.cast(request);
		String uri = httpServletRequest.getRequestURI();
		this.log.info("new request for "+ uri+".");
		
		//proceed with request now just as proceedingjoinpoint
		long time = System.currentTimeMillis();
		chain.doFilter(request, response);
		long delta = System.currentTimeMillis() - time;
		this.log.info("request for "+ uri+ " took "+ delta+ " ms.");
	}
	
}

@Component
@Aspect
class LoggingAspect {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Around("execution( * com.example..*.*(..) )")
	public Object log(ProceedingJoinPoint pjp) throws Throwable {
		//before method invocation
		this.log.info("before " + pjp.toString());
		Object object = pjp.proceed();
		this.log.info("after " + pjp.toString());
		//after method invocation
		
		return object;
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

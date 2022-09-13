package com.example.bootcamp;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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

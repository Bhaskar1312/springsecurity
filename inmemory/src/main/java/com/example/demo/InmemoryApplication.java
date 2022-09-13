package com.example.demo;

import java.security.Principal;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class InmemoryApplication {
	
	@Bean
	UserDetailsService userDetailsService() { //inmemory
		return new InMemoryUserDetailsManager();
	}
	
	@Bean
	InitializingBean initializer(UserDetailsManager manager) {
		return ()-> {
			
			UserDetails bhaskar = User.withDefaultPasswordEncoder().username("bhaskar").password("password").roles("USER").build();
			manager.createUser(bhaskar);
			UserDetails josh = User.withUserDetails(bhaskar).username("josh").build();
			manager.createUser(josh);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(InmemoryApplication.class, args);
	}

}

@RestController
class GreetingsRestController {
	@GetMapping("/greeting")
	String greeting(Principal principal) {
//		principal details present in UserDetails
//		Authentication
//		AuthenticationManager
//		AuthenticationProvider
//		DaoAuthenticationProvider 
		return "hello "+principal.getName() + "!";
	}
}

@SuppressWarnings("deprecation")
@EnableWebSecurity
@Configuration
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.httpBasic();
		http
			.authorizeRequests().anyRequest().authenticated();
	}

	
	
	
}
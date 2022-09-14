package com.example.demo;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class CustomAuthenticationApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomAuthenticationApplication.class, args);
	}

}

@Configuration
@EnableWebSecurity
class CustomSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	private final CustomAuthenticationProvider  ap;
	
	CustomSecurityConfiguration(CustomAuthenticationProvider ap) {
		this.ap = ap;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic(); //for every request, ask for username, password -> even in browser
		http.authorizeRequests().anyRequest().authenticated();
		
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(this.ap);
	}
	
}

@Component
class CustomAuthenticationProvider implements AuthenticationProvider {
	
	private boolean isValid(String user, String pw) {
		// call some other service
		return user.equals("bhaskar") && pw.equals("password");
	}
	private final List<GrantedAuthority> authorities
	= Collections.singletonList(new SimpleGrantedAuthority("USER"));
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		if(isValid(username, password)) {
			return new UsernamePasswordAuthenticationToken(username, password, this.authorities);
		}
		throw new BadCredentialsException("couldn't authenticate using "+ username + "!");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
//	AuthenticationManager 
//	AuthenticationProvider = same interface as authManager, also supports(Class authentication) method
	
}

@RestController
class GreetingRestController {
	
	@GetMapping("/greeting")
	String greet(Principal p) {
		return "greetings " + p.getName() + "!";
	}
}

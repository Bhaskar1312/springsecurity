package com.example.demo;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class PasswordEncodingAndMigrationApplication {
	
	@Bean
	public InMemoryAuditEventRepository repository(){
	 return new InMemoryAuditEventRepository();
	}
	
//	@Bean
	PasswordEncoder oldPasswordEncoder() {
		String md5 = "MD5";
		return new DelegatingPasswordEncoder(md5, Collections.singletonMap(md5, new MessageDigestPasswordEncoder(md5)));
	}
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	@Bean
	CustomUserDetailsService customUserDetailsService() {
		Collection<UserDetails> users = Arrays.asList(
				new CustomUserDetails("bhaskar", oldPasswordEncoder().encode( "password"), true, "USER", "ADMIN"),
				new CustomUserDetails("jlong", oldPasswordEncoder().encode( "password"), true, "USER")
		);
		return new CustomUserDetailsService(users);
	}
	public static void main(String[] args) {
		SpringApplication.run(PasswordEncodingAndMigrationApplication.class, args);
	}

}

@Configuration
@EnableWebSecurity
class CustomSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic(); //for every request, ask for username, password -> even in browser
		http.authorizeRequests().anyRequest().authenticated();
		
	}
	
}

//@Service
@Slf4j
class CustomUserDetailsService implements UserDetailsService, UserDetailsPasswordService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Map<String, UserDetails> users  = new ConcurrentHashMap<>();
	
	public CustomUserDetailsService(Collection<UserDetails> seedUsers) {
		seedUsers.forEach(user -> this.users.put(user.getUsername(), user));
		this.users.forEach((k, v) -> log.info(k+ "="+v.getPassword()));
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if(this.users.containsKey(username)) {
			return this.users.get(username);
		}
		log.error("Username Not Found Exception", UsernameNotFoundException.class);

		throw new UsernameNotFoundException(String.format("couldn't find %s!", username));
	}

	@Override
	public UserDetails updatePassword(UserDetails user, String newPassword) {
		log.info("being prompted to update password for user "+ user.getUsername()+ " to "+ newPassword);
		this.users.put(user.getUsername(), new CustomUserDetails(
			user.getUsername(),
//			user.getPassword(),
			newPassword,
			user.isEnabled(),
			user.getAuthorities()
			.stream().map(ga -> ga.getAuthority())
			.collect(Collectors.toList()).toArray(new String[0])
		));
//		return users.get(user.getUsername());
		return this.loadUserByUsername(user.getUsername());
		//on  curl -vu bhaskar:password http://localhost:8080/greeting, u will see {bcrypt}... on console.log
	}
	
}

class CustomUserDetails implements UserDetails {
	private final Set<GrantedAuthority> authorities;
	private final String username, password;
	private final boolean active;
	
	public CustomUserDetails(String username, String password, boolean active, String...authorties) {
		this.username = username;
		this.password = password;
		this.active = active;
		this.authorities = Stream.of(authorties)
									.map(SimpleGrantedAuthority::new)
									.collect(Collectors.toSet());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.active;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.active;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.active;
	}

	@Override
	public boolean isEnabled() {
		return this.active;
	}
}

@RestController
class GreetingRestController {
	
	@GetMapping("/greeting")
	String greet(Principal p) {
		return "greetings " + p.getName() + "!";
	}
}

package com.example.demo;

import java.security.Principal;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class JdbcApplication {
	
	@Bean
	DataSource dataSource() {
		return new EmbeddedDatabaseBuilder()
			.setType(EmbeddedDatabaseType.H2)
			.addScript(JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION)
			.build();
	}
	
	@Bean
	UserDetailsService userDetailsService(DataSource ds) { 
		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
		jdbcUserDetailsManager.setDataSource(ds);
		return jdbcUserDetailsManager;
	}
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Bean
	InitializingBean initializer(UserDetailsManager manager) {
		return ()-> {
			log.info("Inserting users into database");
			UserDetails bhaskar = User.withDefaultPasswordEncoder().username("bhaskar").password("password").roles("USER").build();
			manager.createUser(bhaskar);
			UserDetails josh = User.withUserDetails(bhaskar).username("josh").build();
			manager.createUser(josh);
			
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(JdbcApplication.class, args);
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
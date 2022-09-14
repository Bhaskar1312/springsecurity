package com.example.demo;

import java.security.Principal;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class LoginApplication {
	
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
		SpringApplication.run(LoginApplication.class, args);
	}

}


@ControllerAdvice
class PrincipalControllerAdvice {
	
	@ModelAttribute("currentUser")
	Principal currentUser(Principal p) {
		return p;
	}
}

@Controller
class LoginController {
//	@GetMapping("/")
//	String index() {
//		return "index";
//	}
	@GetMapping("/")
	String hidden(Model model) {
		return "hidden";
	}
	
	@GetMapping("/login")
	String login() {
		return "login";
	}
	
	@GetMapping("/logout-success")
	String logout() {
		return "logout";
	}
}

@SuppressWarnings("deprecation")
@EnableWebSecurity
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests().anyRequest().authenticated();
		http
			.formLogin().loginPage("/login").permitAll();
		http
			.logout().logoutUrl("/logout")
//			.logoutSuccessHandler(new LogoutSuccessHandler() {
//				
//				@Override
//				public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
//						throws IOException, ServletException {
//					// TODO Auto-generated method stub
//					
//				}
//			});
			.logoutSuccessUrl("/logout-success");
	}
	
}

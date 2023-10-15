package com.example.resourceserver;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().access("principal?.claims['email'] == 'user@example.com'")
                .and()
            .oauth2ResourceServer()
            .jwt();

        //
        // // @formatter:off
        // http
        //     .oauth2Login()
        //         .and()
        //     .authorizeRequests()
        //         .anyRequest().authenticated()
        //         .and()
        //     .oauth2Client()
        //         .authorizationCodeGrant();
        // // @formatter:on
    }
}

package com.example.testingmethodcustomuser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import com.example.testingmethodcustomuser.message.Message;
import com.example.testingmethodcustomuser.user.MessageUser;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TestingMethodCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestingMethodCustomerApplication.class, args);
    }

    @Bean
    Message message() {
        return new Message(bhaskar(), "Hi Bhaskar");
    }

    @Bean
    MessageUser bhaskar() {
        return new MessageUser(1L, "bhaskar@example.com", "password");
    }

    @Bean
    MessageUser rob() {
        return new MessageUser(2L, "rob@example.com", "password");
    }

}

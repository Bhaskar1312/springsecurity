package com.example.testingmethods;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// @RunWith(SpringJUnit4ClassRunner.class) // for junit 4
public class ManualMessageServiceTests {

    @Autowired
    MessageService messageService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void notAuthenticated() {
        assertThatCode(() -> this.messageService.getMessage())
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void notAuthorized() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "password", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThatCode(() -> this.messageService.getMessage())
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void authorized() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "password", "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThatCode(() -> this.messageService.getMessage())
            .doesNotThrowAnyException();
    }
}

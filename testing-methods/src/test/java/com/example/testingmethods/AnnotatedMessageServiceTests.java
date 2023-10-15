package com.example.testingmethods;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class AnnotatedMessageServiceTests {
    @Autowired
    MessageService messageService;


    @Test
    @WithMockUser // default is ROLE_USER
    void notAuthorized() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "password", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThatCode(() -> this.messageService.getMessage())
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void authorized() {
        assertThatCode(() -> this.messageService.getMessage())
            .doesNotThrowAnyException();
    }

    @Test
    @WithAdmin // custom meta annotation
    // WithMockUser is annotated with WithSecurityContext which has a factory WithMockUserSecurityContextFactory that instructs how to create SecurityContext (look at sources)
    void meta() {
        assertThatCode(() -> this.messageService.getMessage())
            .doesNotThrowAnyException();
    }
}

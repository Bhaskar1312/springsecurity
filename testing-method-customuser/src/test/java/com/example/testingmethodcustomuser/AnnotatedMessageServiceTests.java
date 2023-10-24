package com.example.testingmethodcustomuser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithUserDetails;

import com.example.testingmethodcustomuser.message.MessageService;

@SpringBootTest
public class AnnotatedMessageServiceTests {

    @Autowired
    MessageService service;

    @Test
    @WithMockUser("bhaskar@example.com")
    void wrongType() {
        assertThatCode(()-> this.service.getMessage()).isInstanceOf(RuntimeException.class);
    }

    @Test
    @WithUserDetails("bhaskar@example.com")
    void granted() {
        assertThatCode(()-> {
            System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            System.out.println( SecurityContextHolder.getContext().getAuthentication());
            this.service.getMessage();
        }).doesNotThrowAnyException();
    }

    @Test
    @WithUserDetails("rob@example.com")
    void denied() {
        assertThatCode(()-> this.service.getMessage()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockMessageUser
    void grantedWithMocks() {
        assertThatCode(()->this.service.getMessage()).doesNotThrowAnyException();
    }

    @Test
    @WithMockMessageUser(id=2L)
    void deniedWithMocks() {
        assertThatCode(()->this.service.getMessage()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithBhaskar
    void grantedForBhaskar() {
        assertThatCode(()->this.service.getMessage()).doesNotThrowAnyException();
    }

    @Test
    @WithRob
    void deniedForRob() {
        assertThatCode(()->this.service.getMessage()).isInstanceOf(AccessDeniedException.class);
    }
}

package com.example.testingmethods;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@WithAdmin // at class level
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class AnnotatedClassMessageServiceTests {
    @Autowired
    MessageService messageService;

    @Test
    void granted() {
        assertThatCode(()-> messageService.getMessage()).doesNotThrowAnyException();
        // no exception
    }

    @Test
    @WithAnonymousUser // override class level
    void anonymous() {
        assertThatCode(()-> messageService.getMessage()).isInstanceOf(AccessDeniedException.class);

    }


}

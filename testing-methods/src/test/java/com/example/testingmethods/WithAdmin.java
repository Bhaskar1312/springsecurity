package com.example.testingmethods;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(roles="ADMIN")
@Retention(RetentionPolicy.RUNTIME)
public @interface WithAdmin {

}

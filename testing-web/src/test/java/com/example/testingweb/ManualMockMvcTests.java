package com.example.testingweb;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ManualMockMvcTests {

    @Autowired
    WebApplicationContext context;

    @Autowired
    Filter springSecurityFilterChain;

    @Test
    void contextFails() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .addFilters(springSecurityFilterChain)
                .build();

        TestingAuthenticationToken token = new TestingAuthenticationToken("user", "password", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(token);

        mockMvc.perform(get("/"))
            .andExpect(status().isUnauthorized());
        // spring security's SecurityContext PeristentFilter is establishing the session with a securityContext
    }

    @Test
    void contextInSession() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .addFilters(springSecurityFilterChain)
            .build();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        TestingAuthenticationToken token = new TestingAuthenticationToken("user", "password", "ROLE_USER");
        context.setAuthentication(token);

        MockHttpServletRequestBuilder request = get("/")
            .sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        mockMvc.perform(request)
            .andExpect(status().isOk());
    }
}

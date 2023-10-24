package com.example.testingweb;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class MockMvcTests {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
     void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                        .apply(springSecurity()) // add spring security and mock mvc
                        .build();

    }

    @Test
    @WithMockUser
    void allowed() throws Exception {
        this.mockMvc.perform(get("/"))
            .andExpect(status().isOk());
    }

    @Test
    void fail() throws Exception {
        this.mockMvc.perform(get("/"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void allowedWithReqPostProcessor() throws Exception {
        MockHttpServletRequestBuilder request = get("/")
            .with(user("user")); // default role is ROLE_USER
        this.mockMvc.perform(request)
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void postFails() throws Exception {
        MockHttpServletRequestBuilder request = post("/transfer")
                                                    .param("amount", "1");
        mockMvc.perform(request)
            .andExpect(status().isForbidden()); // valid csrf is needed
    }

    @Test
    @WithMockUser
    void postIsRedirected() throws Exception {
        MockHttpServletRequestBuilder request = post("/transfer")
            .param("amount", "1")
            .with(csrf()); // valid csrf is needed
        mockMvc.perform(request)
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void login() throws Exception { // test for form login
        mockMvc.perform(formLogin())
            .andExpect(status().is3xxRedirection())
            .andExpect(authenticated());
    }

    @Test
    void formLoginFails() throws Exception { // test for form login
        mockMvc.perform(formLogin().password("invalid"))
            .andExpect(status().is3xxRedirection())
            .andExpect(unauthenticated());
    }
}

package com.example.testingmethodcustomuser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.example.testingmethodcustomuser.user.MessageUser;
import com.example.testingmethodcustomuser.user.UserRepository;
import com.example.testingmethodcustomuser.user.UserRepositoryUserDetailsService;

public class WithMockCustomUserFactory implements WithSecurityContextFactory<WithMockMessageUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockMessageUser annotation) {
        UserRepository users = mock(UserRepository.class);
        when(users.findByEmail(any())).thenReturn(createUser(annotation));

        UserRepositoryUserDetailsService uds = new UserRepositoryUserDetailsService(users);
        UserDetails principal = uds.loadUserByUsername(annotation.value());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(authentication);
        return context;
    }

    private MessageUser createUser(WithMockMessageUser annotation) {
        return new MessageUser(annotation.id(), annotation.value(), "notused");
    }

}

package com.example.oauth2login;

import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    /**
     * CommonOAuth2Provider , ClientRegistrationRepository, OAuth2AuthorizationRequestRedirectFilter, OAuth2LoginAuthenticationFilter
     * */
    @GetMapping("/user")
    Map<String, Object> user(OAuth2AuthenticationToken authenticationToken) {
        return authenticationToken.getPrincipal().getAttributes();
    }
}

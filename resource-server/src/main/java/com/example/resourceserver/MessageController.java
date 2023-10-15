package com.example.resourceserver;

import jdk.jfr.Registered;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class MessageController {
    @GetMapping("/")
    Map<String, String> message(@AuthenticationPrincipal Jwt jwt) {
        return Collections.singletonMap("text", "Hello "+jwt.getClaimAsString("email"));
    }


    private final WebClient client;

    public MessageController(WebClient client) {
        this.client = client;
    }

    @GetMapping("/message/annotation/keycloak")
    Mono<String> annotationKeyCloak(@RegisteredOAuth2AuthorizedClient("keycloak")
                                    OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        System.out.println(oAuth2AuthorizedClient.getAccessToken().getTokenValue());
        return this.client.get().uri("http://localhost:9090/")
            .attributes(oAuth2AuthorizedClient(oAuth2AuthorizedClient))
            .retrieve()
            .bodyToMono(String.class);
    }

    @GetMapping("/message/keycloak")
    Mono<String> annotationKeyCloak() {
        return this.client.get().uri("http://localhost:9090/")
            .attributes(clientRegistrationId("keycloak"))
            .retrieve()
            .bodyToMono(String.class);
    }

    @GetMapping("/message")
    Mono<String> implied() {
        return this.client.get()
            .uri("http://localhost:9090/")
            .retrieve()
            .bodyToMono(String.class);
    }

}

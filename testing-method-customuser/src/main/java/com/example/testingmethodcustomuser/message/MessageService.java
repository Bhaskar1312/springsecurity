package com.example.testingmethodcustomuser.message;

import org.springframework.security.access.prepost.PostAuthorize;

public interface MessageService {

    @PostAuthorize("returnObject?.to?.id == principal?.id")
    // @PostAuthorize("returnObject?.to?.id == principal?.username")
    Message getMessage();
}

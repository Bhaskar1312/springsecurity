package com.example.testingmethodcustomuser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockMessageUser(id=2L)
public @interface WithRob {

}

package com.example.settlementnew.aop;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({java.lang.annotation.ElementType.METHOD})
@Retention(RUNTIME)
public @interface SendStartMessage {

    String title();
    String detail();
}

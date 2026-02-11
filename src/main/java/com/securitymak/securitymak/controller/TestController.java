package com.securitymak.securitymak.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/secure")
    public String secure(Authentication authentication) {
        return "Hello " + authentication.getName() + ", you are authenticated!";
    }
}


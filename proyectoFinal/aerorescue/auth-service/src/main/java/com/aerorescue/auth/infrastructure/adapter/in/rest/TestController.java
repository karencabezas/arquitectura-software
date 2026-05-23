package com.aerorescue.auth.infrastructure.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/hash")
    public String hash() {
        System.out.println(passwordEncoder.encode("Admin123!"));
        return passwordEncoder.encode("Admin123!");
    }
}

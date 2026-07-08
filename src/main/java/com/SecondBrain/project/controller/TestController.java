package com.SecondBrain.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is public — no token needed!");
    }

    @GetMapping("/private")
    public ResponseEntity<String> privateEndpoint(Authentication auth) {

        return ResponseEntity.ok("Hello " + auth.getName() + "! Your JWT worked.");
    }
}
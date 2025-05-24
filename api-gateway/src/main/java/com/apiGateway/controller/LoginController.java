package com.apiGateway.controller;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private SecretKey jwtSecretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostMapping("/login")
    public Mono<Map<String, String>> createAuthenticationToken(@RequestBody LoginRequest loginRequest) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        ).map(authentication -> {
            String jwt = Jwts.builder()
                    .subject(authentication.getName())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(jwtSecretKey)
                    .compact();

            Map<String, String> response = new HashMap<>();
            response.put("bearerToken", jwt);
            response.put("username", authentication.getName());
            response.put("roles", authentication.getAuthorities().toString());
            return response;
        });
    }
}

    /**
     ** User Credentials
    1)  Username: admin,
        Password: admin123,
        Role: ADMIN
    2)  Username: user,
        Password: user123,
        Role: USER
    */

class LoginRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


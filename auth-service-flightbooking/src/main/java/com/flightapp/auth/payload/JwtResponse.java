package com.flightapp.auth.payload;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String username;
    private String email;
    private Set<String> roles;
}

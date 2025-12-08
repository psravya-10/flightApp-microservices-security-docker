package com.flightapp.auth.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flightapp.auth.model.User;
import com.flightapp.auth.payload.LoginRequest;
import com.flightapp.auth.payload.SignupRequest;
import com.flightapp.auth.payload.JwtResponse;
import com.flightapp.auth.repository.UserRepository;
import com.flightapp.auth.security.CustomUserDetails;
import com.flightapp.auth.security.JwtUtils;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public void register(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already in use");
        }

        Set<String> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            roles.add("USER"); 
        } else {
            
            for (String r : request.getRoles()) {
                if ("ADMIN".equalsIgnoreCase(r)) roles.add("ADMIN");
                else roles.add("USER");
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = User.builder()
                .id(null)
                .username(principal.getDisplayName())
                .email(principal.getUsername())
                .roles(principal.getRoles())
                .build();

        String token = jwtUtils.generateToken(user);
        return new JwtResponse(token, user.getUsername(), user.getEmail(), user.getRoles());
    }
}

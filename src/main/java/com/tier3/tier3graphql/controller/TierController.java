package com.tier3.tier3graphql.controller;

import com.tier3.tier3graphql.DTO.LoginRequest;
import com.tier3.tier3graphql.DTO.LoginResponse;
import com.tier3.tier3graphql.DTO.Response;
import com.tier3.tier3graphql.config.security.BadCredentialsException;
import com.tier3.tier3graphql.config.security.EmailNotVerifiedException;
import com.tier3.tier3graphql.model.User;
import com.tier3.tier3graphql.repository.HomeRepository;
import com.tier3.tier3graphql.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class TierController {

    private final UserService userService;
    private final HomeRepository homeRepository;

    private final AuthenticationProvider authenticationProvider;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        try {
            User user1 = userService.getUser(loginRequest.getUsername());

            if (user1.isEnabled()) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                Authentication authentication = authenticationProvider.authenticate(credentials);
                securityContext.setAuthentication(authentication);
                User user = userService.getCurrentUser();
                return ResponseEntity.ok(new LoginResponse(userService.getToken(user), user));
            } else {
                throw new EmailNotVerifiedException();
            }
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException();
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        user.setEnabled(true);
        userService.addUser(user);
        System.out.println(user);
        return ResponseEntity.ok(new Response(true, "Please verify your email to continue"));
    }

    @GetMapping("/auth/account-info")
    public ResponseEntity<?> getUserInfo() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

}

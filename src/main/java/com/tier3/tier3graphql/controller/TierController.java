package com.tier3.tier3graphql.controller;

import com.tier3.tier3graphql.DTO.LoginRequest;
import com.tier3.tier3graphql.DTO.LoginResponse;
import com.tier3.tier3graphql.DTO.Response;
import com.tier3.tier3graphql.config.security.BadCredentialsException;
import com.tier3.tier3graphql.config.security.EmailNotVerifiedException;
import com.tier3.tier3graphql.model.User;
import com.tier3.tier3graphql.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
public class TierController {

    private final UserService userService;
    private final AuthenticationProvider authenticationProvider;

    @QueryMapping
    public String Welcome() {
        return "Welcome to Tier3 engineers Api";
    }

    @QueryMapping
    User GetCurrentUser () {
        return userService.getCurrentUser();
    }

    @QueryMapping
    ArrayList<User> GetAllUsers () {
        return (ArrayList<User>) userService.getUsers();
    }

    @MutationMapping
    LoginResponse Login(@Argument("input") LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        try {
            User user1 = userService.getUser(loginRequest.getUsername());

            if (user1.isEnabled()) {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                Authentication authentication = authenticationProvider.authenticate(credentials);
                securityContext.setAuthentication(authentication);
                User user = userService.getCurrentUser();
                return new LoginResponse(userService.getToken(user), user);
            } else {
                throw new EmailNotVerifiedException();
            }
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException();
        }

    }

    @MutationMapping
    Response CreateUser(@Argument("input") User user) {
        user.setEnabled(true);
        userService.addUser(user);
        //userService.createEmailToken(user.getEmail());
        return new Response(true, "Please verify your email to continue");
    }

}

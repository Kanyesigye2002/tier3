package com.tier3.tier3graphql.controller;

import com.tier3.tier3graphql.DTO.LoginRequest;
import com.tier3.tier3graphql.DTO.LoginResponse;
import com.tier3.tier3graphql.DTO.Response;
import com.tier3.tier3graphql.config.security.BadCredentialsException;
import com.tier3.tier3graphql.config.security.EmailNotVerifiedException;
import com.tier3.tier3graphql.model.HomePage;
import com.tier3.tier3graphql.model.User;
import com.tier3.tier3graphql.model.util.*;
import com.tier3.tier3graphql.repository.HomeRepository;
import com.tier3.tier3graphql.repository.util.MessageRepository;
import com.tier3.tier3graphql.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
public class TierGraphQLController {

    private final UserService userService;
    private final HomeRepository homeRepository;
    private final MessageRepository messageRepository;

    private final AuthenticationProvider authenticationProvider;

    @QueryMapping
    public String Welcome() {
        return "Welcome to Tier3 engineers Api";
    }

    // Auth
    // --------------------------------------------------------------------------------------------

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
        return ResponseEntity.ok(new Response(true, "Please verify your email to continue"));
    }

    //User Controllers
    //-------------------------------------------------------------------------------------------

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
    
    @MutationMapping
    Response UpdateUser(@Argument("input") User user) {
        userService.UpdateUserInfo(user);
        return new Response(true, "Please verify your email to continue");
    }

    @MutationMapping
    Response CreateMailVerification(@Argument("input") String userName) {
        userService.createEmailToken(userName);
        return new Response(true, "An email has been sent for account activation");
    }

    @MutationMapping
    Response CreateNewPassword(@Argument("input") String password) {
        userService.createConfirmPasswordToken(password);
        return new Response(true, "An email has been sent for new password confirmation");
    }

    @MutationMapping
    Response CreateForgotPassword(@Argument("input") String userName) {
        userService.createForgotPasswordToken(userName);
        return new Response(true, "An email has been sent for password reset");
    }

    @MutationMapping
    Response VerifyEmail(@Argument("input") String token) {
        userService.verifyEmail(token);
        return new Response(true, "Account has been activated successfully");
    }

    @MutationMapping
    Response VerifyNewPassword(@Argument("input") String token) {
        userService.updateConfirmPassword(token);
        return new Response(true, "Password was successfully updated");
    }

    @MutationMapping
    Response VerifyForgotPassword(@Argument("input") String token, @Argument("password") String password) {
        userService.verifyForgotPassword(token, password);
        return new Response(true, "Password was successfully updated");
    }

    //Home Controllers
    //-------------------------------------------------------------------------------------------

    @QueryMapping
    HomePage GetHomePage () {
        return homeRepository.findById(1L).get();
    }

    @MutationMapping
    Response CreateHome(@Argument("input") HomePage home) {
        homeRepository.save(home);
        return new Response(true, "Home has been created/updated successfully");
    }

    //Contact Us Controllers
    //-------------------------------------------------------------------------------------------

    @QueryMapping
    ArrayList<Message> GetAllMessages () {
        return (ArrayList<Message>) messageRepository.findAll();
    }

    @MutationMapping
    Message ContactUs(@Argument("input") Message message) {
        return messageRepository.save(message);
    }

    //Events Controllers
    //-------------------------------------------------------------------------------------------

    @QueryMapping
    Event GetEventById (@Argument("input") long id) {
        return userService.GetEventById(id);
    }

    @QueryMapping
    ArrayList<Event> GetAllEvents () {
        return userService.GetAllEvents();
    }

    @MutationMapping
    Event CreateEvent(@Argument("input") Event event) {
        return userService.CreateEvent(event);
    }

    @MutationMapping
    Event UpdateEvent(@Argument("input") Event event) {
        return userService.UpdateEvent(event);
    }

    //Projects Controllers
    //-------------------------------------------------------------------------------------------

    @QueryMapping
    Project GetProjectById (@Argument("input") long id) {
        return userService.GetProjectById(id);
    }

    @QueryMapping
    ArrayList<Project> GetAllProjects () {
        return userService.GetAllProjects();
    }

    @MutationMapping
    Project CreateProject(@Argument("input") Project project) {
        return userService.CreateProject(project);
    }

    @MutationMapping
    Project UpdateProject(@Argument("input") Project project) {
        return userService.UpdateProject(project);
    }

    //Specializations Controllers
    //-------------------------------------------------------------------------------------------

    @QueryMapping
    Specialization GetSpecializationById (@Argument("input") long id) {
        return userService.GetSpecializationById(id);
    }

    @QueryMapping
    ArrayList<Specialization> GetAllSpecializations () {
        return userService.GetAllSpecializations();
    }

    @MutationMapping
    Specialization CreateSpecialization(@Argument("input") Specialization specialization) {
        return userService.CreateSpecialization(specialization);
    }

    @MutationMapping
    Specialization UpdateSpecialization(@Argument("input") Specialization specialization) {
        return userService.UpdateSpecialization(specialization);
    }

    //JobHistories Controllers
    //-------------------------------------------------------------------------------------------

    @QueryMapping
    JobHistory GetJobHistoryById (@Argument("input") long id) {
        return userService.GetJobHistoryById(id);
    }

    @QueryMapping
    ArrayList<JobHistory> GetAllJobHistories () {
        return userService.GetAllJobHistories();
    }

    @MutationMapping
    JobHistory CreateJobHistory(@Argument("input") JobHistory jobHistory) {
        return userService.CreateJobHistory(jobHistory);
    }

    @MutationMapping
    JobHistory UpdateJobHistory(@Argument("input") JobHistory jobHistory) {
        return userService.UpdateJobHistory(jobHistory);
    }

    //AcademicHistories Controllers
    //-------------------------------------------------------------------------------------------

    @QueryMapping
    AcademicHistory GetAcademicHistoryById (@Argument("input") long id) {
        return userService.GetAcademicHistoryById(id);
    }

    @QueryMapping
    ArrayList<AcademicHistory> GetAllAcademicHistories () {
        return userService.GetAllAcademicHistories();
    }

    @MutationMapping
    AcademicHistory CreateAcademicHistory(@Argument("input") AcademicHistory jobHistory) {
        return userService.CreateAcademicHistory(jobHistory);
    }

    @MutationMapping
    AcademicHistory UpdateAcademicHistory(@Argument("input") AcademicHistory jobHistory) {
        return userService.UpdateAcademicHistory(jobHistory);
    }
    
    //Verification Controllers
    //-------------------------------------------------------------------------------------------

}

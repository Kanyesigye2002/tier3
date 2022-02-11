package com.tier3.tier3graphql.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tier3.tier3graphql.config.security.*;
import com.tier3.tier3graphql.model.util.EmailVerification;
import com.tier3.tier3graphql.model.util.ForgotPassword;
import com.tier3.tier3graphql.model.util.NewPassword;
import com.tier3.tier3graphql.model.User;
import com.tier3.tier3graphql.repository.util.EmailVerificationRepository;
import com.tier3.tier3graphql.repository.util.ForgotPasswordRepository;
import com.tier3.tier3graphql.repository.util.NewPasswordRepository;
import com.tier3.tier3graphql.repository.UserRepository;
import com.tier3.tier3graphql.service.MailServices.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    private final JWTVerifier verifier;
    private final PasswordEncoder passwordEncoder;
    private final NewPasswordRepository newPasswordRepository;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService mailService;


    private final Algorithm algorithm;
    private final SecurityProperties properties;

    public String getToken(User user) {
        Instant now = Instant.now();
        Instant expiry = Instant.now().plus(properties.getTokenExpiration());
        return JWT
                .create()
                .withIssuer(properties.getTokenIssuer())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiry))
                .withSubject(user.getUsername())
                .sign(algorithm);
    }

    @Override
    public JWTUserDetails loadUserByUsername(String username) throws UserNotFoundException {

        return userRepository
                .findByUsername(username)
                .map(user -> getUserDetails(user, getToken(user)))
                .orElseThrow(() -> new UsernameNotFoundException("Username or password didn''t match"));

    }

    public JWTUserDetails loadUserByToken(String token) {
        return getDecodedToken(token)
                .map(DecodedJWT::getSubject)
                .flatMap(userRepository::findByUsername)
                .map(user -> getUserDetails(user, token))
                .orElseThrow(BadTokenException::new);
    }

    public String createEmailToken(String userName) {
        User user = getUser(userName);
        String token = getToken(user);
        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setUser(user);
        emailVerification.setToken(token);

        emailVerificationRepository.save(emailVerification);

        mailService.sendVerificationToken(token, user);

        return token;
    }

    public User verifyEmail(String token) {

        getDecodedToken(token);

        EmailVerification emailVerification = emailVerificationRepository.findByToken(token);

        if (emailVerification != null) {
            User user = emailVerification.getUser();
            user.setEnabled(true);
            return user;
        } else {
            throw new BadTokenException();
        }

    }

    public NewPassword createConfirmPasswordToken(String password) {
        User user = getCurrentUser();
        String token = getToken(user);
        NewPassword newPassword = new NewPassword();
        newPassword.setPassword(passwordEncoder.encode(password));
        newPassword.setToken(token);
        newPassword.setUser(user);

        mailService.sendConfirmPasswordChangeToken(token, user);

        return newPasswordRepository.save(newPassword);
    }

    public User updateConfirmPassword(String token) {
        NewPassword newPassword = newPasswordRepository.findByToken(token);
        User user = newPassword.getUser();
        user.setPassword(newPassword.getPassword());
        return newPassword.getUser();
    }

    public String createForgotPasswordToken(String userName) {
        User user = getUser(userName);
        String token = getToken(user);
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setUser(user);
        forgotPassword.setToken(token);

        mailService.sendForgotPasswordToken(token, user);

        return token;
    }

    public User verifyForgotPassword(String token, String password) {
        User user = forgotPasswordRepository.findByToken(token).getUser();
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }

    public User getCurrentUser() {

        return Optional
                .ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(userRepository::findByUsername)
                .orElse(null);
    }

    public User addUser(User user) {
        if (!exists(user)) {

            user.setUsername(user.getUsername());
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            return userRepository.save(user);
        } else {
            throw new UserAlreadyExistsException();
        }

    }

    public User UpdateUserInfo(User user) {
        User originalUser = getUser(user.getId());
        originalUser.setAddress(user.getAddress());
        originalUser.setCity(user.getCity());
        originalUser.setCountry(user.getCountry());
        return originalUser;
    }

    public Iterable<User> addAllUser(List<User> usersIn) {
        Iterable<User> users = new ArrayList<>(usersIn);
        return userRepository.saveAll(users);
    }

    public List<User> getUsers() {
        return StreamSupport
                .stream(userRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User getUser(String userName) {
        Optional<User> user = userRepository.findByUsername(userName);
        if (user.isEmpty())
            throw new UserNotFoundException();
        else
            return user.get();
    }

    public User deleteUser(Long id) {
        User user = getUser(id);
        userRepository.delete(user);
        return user;
    }

    private boolean exists(User user) {
        return userRepository.existsByUsername(user.getUsername());
    }

    private JWTUserDetails getUserDetails(User user, String token) {

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        user.getAuthorities().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getRoleCode()));
        });

        return JWTUserDetails
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .enabled(user.isEnabled())
                .authorities(authorities)
                .token(token)
                .build();
    }

    private Optional<DecodedJWT> getDecodedToken(String token) {
        try {
            return Optional.of(verifier.verify(token));
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }
}


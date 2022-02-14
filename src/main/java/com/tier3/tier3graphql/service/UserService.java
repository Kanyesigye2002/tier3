package com.tier3.tier3graphql.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tier3.tier3graphql.config.security.*;
import com.tier3.tier3graphql.model.User;
import com.tier3.tier3graphql.model.util.*;
import com.tier3.tier3graphql.repository.UserRepository;
import com.tier3.tier3graphql.repository.util.*;
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
    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;
    private final AcademicHistoryRepository academicHistoryRepository;
    private final JobHistoryRepository jobHistoryRepository;
    private final SpecializationRepository specializationRepository;
    private final NotificationRepository notificationRepository;

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

    //Events Services
    //-------------------------------------------------------------------------------------------v
    
    public Event GetEventById (long id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent()) {
            if (getCurrentUser().getEvents().contains(event)) {
                return event.get();
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }
    
    public ArrayList<Event> GetAllEvents () {
        return (ArrayList<Event>) getCurrentUser().getEvents();
    }
    
    public Event CreateEvent(Event event) {
        Event eventSaved = eventRepository.save(event);
        getCurrentUser().getEvents().add(eventSaved);
        return eventSaved;
    }
    
    public Event UpdateEvent(Event event) {

        Optional<Event> eventOptional = eventRepository.findById(event.getId());
        if (eventOptional.isPresent()) {
            if (getCurrentUser().getEvents().contains(eventOptional.get())) {
                Event event1 = eventOptional.get();
                event1.setTitle(event.getTitle());
                event1.setDescription(event.getDescription());
                event1.setAllDay(event.getAllDay());
                event1.setColor(event.getColor());
                event1.setStart(event.getStart());
                event1.setEnd(event.getEnd());
                return event1;
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }

    //Projects Services
    //-------------------------------------------------------------------------------------------v
    
    public Project GetProjectById (long id) {
        Optional<Project> project = projectRepository.findById(id);
        if (project.isPresent()) {
            if (getCurrentUser().getProjects().contains(project)) {
                return project.get();
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }
    
    public ArrayList<Project> GetAllProjects () {
        return (ArrayList<Project>) getCurrentUser().getProjects();
    }
    
    public Project CreateProject(Project project) {
        Project projectSaved = projectRepository.save(project);
        getCurrentUser().getProjects().add(projectSaved);
        return projectSaved;
    }
    
    public Project UpdateProject(Project project) {

        Optional<Project> projectOptional = projectRepository.findById(project.getId());
        if (projectOptional.isPresent()) {
            if (getCurrentUser().getProjects().contains(projectOptional.get())) {
                Project project1 = projectOptional.get();
                project1.setTitle(project.getTitle());
                project1.setDescription(project.getDescription());
                project1.setCategory(project.getCategory());
                project1.setImageUrl(project.getImageUrl());
                project1.setSkillRate(project.getSkillRate());
                project1.setSkillRate(project.getSkillRate());
                project1.setStart(project.getStart());
                project1.setEnd(project.getEnd());
                return project1;
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }

    //Specializations Services
    //-------------------------------------------------------------------------------------------v
    
    public Specialization GetSpecializationById (long id) {
        Optional<Specialization> specialization = specializationRepository.findById(id);
        if (specialization.isPresent()) {
            if (getCurrentUser().getSpecializations().contains(specialization)) {
                return specialization.get();
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }
    
    public ArrayList<Specialization> GetAllSpecializations () {
        return (ArrayList<Specialization>) getCurrentUser().getSpecializations();
    }
    
    public Specialization CreateSpecialization(Specialization specialization) {
        Specialization specializationSaved = specializationRepository.save(specialization);
        getCurrentUser().getSpecializations().add(specializationSaved);
        return specializationSaved;
    }
    
    public Specialization UpdateSpecialization(Specialization specialization) {

        Optional<Specialization> specializationOptional = specializationRepository.findById(specialization.getId());
        if (specializationOptional.isPresent()) {
            if (getCurrentUser().getSpecializations().contains(specializationOptional.get())) {
                Specialization specialization1 = specializationOptional.get();
                specialization1.setSpeciality(specialization.getSpeciality());
                specialization1.setDescription(specialization.getDescription());
                specialization1.setYears(specialization.getYears());
                return specialization1;
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }

    //JobHistories Services
    //-------------------------------------------------------------------------------------------v
    
    public JobHistory GetJobHistoryById (long id) {
        Optional<JobHistory> jobHistory = jobHistoryRepository.findById(id);
        if (jobHistory.isPresent()) {
            if (getCurrentUser().getJobHistories().contains(jobHistory)) {
                return jobHistory.get();
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }
    
    public ArrayList<JobHistory> GetAllJobHistories () {
        return (ArrayList<JobHistory>) getCurrentUser().getJobHistories();
    }
    
    public JobHistory CreateJobHistory(JobHistory jobHistory) {
        JobHistory jobHistorySaved = jobHistoryRepository.save(jobHistory);
        getCurrentUser().getJobHistories().add(jobHistorySaved);
        return jobHistorySaved;
    }
    
    public JobHistory UpdateJobHistory(JobHistory jobHistory) {

        Optional<JobHistory> jobHistoryOptional = jobHistoryRepository.findById(jobHistory.getId());
        if (jobHistoryOptional.isPresent()) {
            if (getCurrentUser().getJobHistories().contains(jobHistoryOptional.get())) {
                JobHistory jobHistory1 = jobHistoryOptional.get();
                jobHistory1.setTitle(jobHistory.getTitle());
                jobHistory1.setDescription(jobHistory.getDescription());
                jobHistory1.setCategory(jobHistory.getCategory());
                jobHistory1.setStart(jobHistory.getStart());
                jobHistory1.setStatus(jobHistory.getStatus());
                jobHistory1.setEnd(jobHistory.getEnd());
                return jobHistory1;
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }

    //AcademicHistories Services
    //-------------------------------------------------------------------------------------------v
    
    public AcademicHistory GetAcademicHistoryById (long id) {
        Optional<AcademicHistory> academicHistory = academicHistoryRepository.findById(id);
        if (academicHistory.isPresent()) {
            if (getCurrentUser().getAcademicHistories().contains(academicHistory)) {
                return academicHistory.get();
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }
    
    public ArrayList<AcademicHistory> GetAllAcademicHistories () {
        return (ArrayList<AcademicHistory>) getCurrentUser().getAcademicHistories();
    }
    
    public AcademicHistory CreateAcademicHistory(AcademicHistory academicHistory) {
        AcademicHistory academicHistorySaved = academicHistoryRepository.save(academicHistory);
        getCurrentUser().getAcademicHistories().add(academicHistorySaved);
        return academicHistorySaved;
    }
    
    public AcademicHistory UpdateAcademicHistory(AcademicHistory academicHistory) {

        Optional<AcademicHistory> academicHistoryOptional = academicHistoryRepository.findById(academicHistory.getId());
        if (academicHistoryOptional.isPresent()) {
            if (getCurrentUser().getAcademicHistories().contains(academicHistoryOptional.get())) {
                AcademicHistory academicHistory1 = academicHistoryOptional.get();
                academicHistory1.setTitle(academicHistory.getTitle());
                academicHistory1.setDescription(academicHistory.getDescription());
                return academicHistory1;
            } else {
                throw new AccessDeniedException();
            }
        } else {
            throw new ItemNotFoundException();
        }
    }
    
    //User Services
    //-------------------------------------------------------------------------------------------

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


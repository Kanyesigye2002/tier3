package com.tier3.tier3graphql.model;

import com.tier3.tier3graphql.model.util.*;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String firstName;
    private String lastName;
    private String password;
    private String address;
    private String country;
    private String state;
    private String city;
    private String username;
    private String zipCode;
    private String about;
    private boolean enabled = false;

    @ManyToMany()
    private Collection<Authority> authorities = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<Test> tests = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<StringComp> skills = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<Specialization> specializations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<AcademicHistory> academicHistories = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<JobHistory> jobHistories = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<Project> projects = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<Notification> notifications = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Collection<NewPassword> newPasswords = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Collection<ForgotPassword> forgotPasswords = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Collection<EmailVerification> emailVerifications = new ArrayList<>();
}

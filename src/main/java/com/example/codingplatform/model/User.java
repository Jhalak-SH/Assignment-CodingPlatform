package com.example.codingplatform.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @ElementCollection
    @CollectionTable(name = "user_solved_problems")
    private Set<Long> solvedProblems = new HashSet<>();

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Set<Long> getSolvedProblems() {
        return solvedProblems;
    }

    public void setSolvedProblems(Set<Long> solvedProblems) {
        this.solvedProblems = solvedProblems;
    }
}
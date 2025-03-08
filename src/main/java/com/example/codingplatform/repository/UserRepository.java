package com.example.codingplatform.repository;

import com.example.codingplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);  // Add this method
    boolean existsByEmail(String email);
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
            "WHERE LOWER(u.username) LIKE CONCAT('%', LOWER(:username), '%') AND u.isAdmin = true")
    boolean existsAdminWithSimilarUsername(@Param("username") String username);
}
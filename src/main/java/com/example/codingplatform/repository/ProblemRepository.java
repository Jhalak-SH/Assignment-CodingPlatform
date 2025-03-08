package com.example.codingplatform.repository;

import com.example.codingplatform.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    @Query("SELECT COUNT(u) FROM User u WHERE :problemId MEMBER OF u.solvedProblems")
    long countSolvedBy(@Param("problemId") Long problemId);

    boolean existsByNameIgnoreCase(String name);
}
package com.example.codingplatform.service;

import com.example.codingplatform.model.Problem;
import com.example.codingplatform.model.User;
import com.example.codingplatform.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class ProblemService {
    @Autowired
    private ProblemRepository problemRepository;

    public List<Problem> getProblemsByAdmin(Long adminId) {
        List<Problem> problems = problemRepository.findAll();
        return problems.stream()
                .filter(p -> p.getCreatedBy().equals(adminId))
                .peek(p -> p.setSolvedByCount(problemRepository.countSolvedBy(p.getId())))
                .collect(Collectors.toList());
    }


    public Problem addProblem(Problem problem, User admin) {
        if (!problem.isValid()) {
            throw new IllegalArgumentException("Invalid problem details");
        }

        // Check for duplicate problem name
        if (problemRepository.existsByNameIgnoreCase(problem.getName())) {
            throw new IllegalStateException("A problem with this name already exists");
        }

        problem.setCreatedBy(admin.getId());
        return problemRepository.save(problem);
    }

    public List<Problem> getUnsolvedProblems(User user) {
        List<Problem> problems = problemRepository.findAll();

        problems.forEach(p -> p.setSolvedByCount(
                problemRepository.countSolvedBy(p.getId())
        ));

        return problems.stream()
                .filter(p -> !user.getSolvedProblems().contains(p.getId()))
                .filter(p -> !(user.isAdmin() && p.getCreatedBy().equals(user.getId())))
                .collect(Collectors.toList());
    }

    public List<Problem> filterProblems(String level, String tags, User user) {
        if (user == null) {
            throw new IllegalStateException("User is not logged in. Please log in to view problems.");
        }

        System.out.println("Filtering problems for user: " + user.getUsername() +
                ", Level: " + level + ", Tags: " + tags);

        List<Problem> problems = problemRepository.findAll();
        System.out.println("Total problems in DB: " + problems.size());

        List<Problem> filtered = problems.stream()
                .filter(p -> !user.getSolvedProblems().contains(p.getId()))  // Remove solved problems
                .filter(p -> !(user.isAdmin() && p.getCreatedBy().equals(user.getId()))) // Admins can't see their own problems
                .filter(problem -> (level == null || problem.getLevel().equalsIgnoreCase(level))) // Filter by level
                .filter(problem -> (tags == null || problem.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tags)))) // Filter by tags
                .collect(Collectors.toList());

        filtered.forEach(p -> p.setSolvedByCount(problemRepository.countSolvedBy(p.getId())));

        System.out.println("Filtered problems count: " + filtered.size());
        return filtered;
    }

    public List<Problem> getSolvedProblems(User user) {
        List<Problem> allProblems = problemRepository.findAll();
        // Filter problems that the user has solved
        List<Problem> solvedProblems = allProblems.stream()
                .filter(p -> user.getSolvedProblems().contains(p.getId()))
                .collect(Collectors.toList());
        // Set the solved count for each problem
        solvedProblems.forEach(p -> p.setSolvedByCount(
                problemRepository.countSolvedBy(p.getId())
        ));
        return solvedProblems;
    }


    public Problem updateProblem(Long id, Problem updatedProblem, User admin) {
        Problem existingProblem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Problem not found with id: " + id));

        // Check if the admin is the creator of the problem
        if (!existingProblem.getCreatedBy().equals(admin.getId())) {
            throw new IllegalStateException("You can only update problems you created");
        }

        // Validate the updated problem
        if (!updatedProblem.isValid()) {
            throw new IllegalArgumentException("Invalid problem details");
        }

        // Check for name conflict (only if name is being changed)
        if (!existingProblem.getName().equalsIgnoreCase(updatedProblem.getName()) &&
                problemRepository.existsByNameIgnoreCase(updatedProblem.getName())) {
            throw new IllegalStateException("A problem with this name already exists");
        }

        // Update the existing problem
        existingProblem.setName(updatedProblem.getName());
        existingProblem.setDescription(updatedProblem.getDescription());
        existingProblem.setLevel(updatedProblem.getLevel());
        existingProblem.setTags(updatedProblem.getTags());
        // Don't update createdBy field
        return problemRepository.save(existingProblem);
    }

    public void deleteProblem(Long id, User admin) {
            Problem problem = problemRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Problem not found with id: " + id));

            // Check if the admin is the creator of the problem
            if (!problem.getCreatedBy().equals(admin.getId())) {
                throw new IllegalStateException("You can only delete problems you created");
            }

            problemRepository.deleteById(id);
        }


}
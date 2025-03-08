//package com.example.codingplatform.service;
//
//import com.example.codingplatform.model.Problem;
//import com.example.codingplatform.model.User;
//import com.example.codingplatform.repository.ProblemRepository;
//import com.example.codingplatform.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class UserService {
//    @Autowired
//    private UserRepository userRepository;
//
//    public User findByEmail(String email) {
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email));
//    }
//
//    @Autowired
//    private ProblemRepository problemRepository;
//
//
//
//    public List<User> getAllAdmins() {
//        return userRepository.findAll().stream()
//                .filter(User::isAdmin)
//                .collect(Collectors.toList());
//    }
//
//    public List<User> getAllUsers() {
//        return userRepository.findAll().stream()
//                .filter(user -> !user.isAdmin())
//                .collect(Collectors.toList());
//    }
//
//    private void validateEmail(String email) {
//        if (email == null || email.isEmpty()) {
//            throw new IllegalArgumentException("Email cannot be empty");
//        }
//
//        // Check for @ symbol and proper email format
//        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
//            throw new IllegalArgumentException("Invalid email format. Email must contain @ symbol in the correct position");
//        }
//    }
//
//    // Update the existing createUser method
//    public User createUser(String username, String email, Boolean isAdmin) {
//        validateEmail(email);
//
//        if (isAdmin) {
//            if (userRepository.existsAdminWithSimilarUsername(username.toLowerCase())) {
//                throw new IllegalStateException("An admin with a similar username already exists");
//            }
//        }
//
//        if (userRepository.existsByEmail(email)) {
//            throw new IllegalStateException("Email already in use");
//        }
//
//        User user = new User();
//        user.setUsername(username);
//        user.setEmail(email);
//        user.setAdmin(isAdmin);
//        return userRepository.save(user);
//    }
//
//    public User findByUsername(String username) {
//        return userRepository.findByUsername(username)
//                .orElseThrow(() -> new IllegalStateException("User not found"));
//    }
//
//    public void markProblemSolved(User user, Long problemId) {
//        // Check if problem is already solved
//        if (user.getSolvedProblems().contains(problemId)) {
//            throw new IllegalStateException("You have already solved this problem");
//        }
//
//        // Check if admin is trying to solve their own problem
//        Problem problem = problemRepository.findById(problemId)
//                .orElseThrow(() -> new IllegalStateException("Problem not found"));
//
//        if (user.isAdmin() && problem.getCreatedBy().equals(user.getId())) {
//            throw new IllegalStateException("Admins cannot solve their own problems");
//        }
//
//        user.getSolvedProblems().add(problemId);
//        userRepository.save(user);
//    }
//
//    public List<Problem> filterProblems(String level, String tags, User user) {
//        System.out.println("Filtering problems for user: " + user.getUsername() +
//                ", Level: " + level + ", Tags: " + tags);
//
//        List<Problem> problems = problemRepository.findAll();
//        System.out.println("Total problems in DB: " + problems.size());
//
//        List<Problem> filtered = problems.stream()
//                .filter(p -> !user.getSolvedProblems().contains(p.getId()))  // Only unsolved problems
//                .filter(p -> !(user.isAdmin() && p.getCreatedBy().equals(user.getId()))) // Admins can't see their own problems
//                .filter(problem -> (level == null || problem.getLevel().equalsIgnoreCase(level))) // Filter by level
//                .filter(problem -> (tags == null || problem.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tags)))) // Filter by tags
//                .collect(Collectors.toList());
//
//        filtered.forEach(p -> p.setSolvedByCount(problemRepository.countSolvedBy(p.getId())));
//
//        System.out.println("Filtered problems count: " + filtered.size());
//        return filtered;
//    }
//
//
//}


package com.example.codingplatform.service;

import com.example.codingplatform.model.Problem;
import com.example.codingplatform.model.User;
import com.example.codingplatform.repository.ProblemRepository;
import com.example.codingplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProblemRepository problemRepository;

    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalStateException("User is not logged in. Please log in to continue.");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email + ". Please log in to continue."));
    }

    public List<User> getAllAdmins() {
        return userRepository.findAll().stream()
                .filter(User::isAdmin)
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isAdmin())
                .collect(Collectors.toList());
    }

    private void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format. Email must contain @ symbol in the correct position");
        }
    }

    public User createUser(String username, String email, Boolean isAdmin) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        validateEmail(email);

        if (isAdmin) {
            if (userRepository.existsAdminWithSimilarUsername(username.toLowerCase())) {
                throw new IllegalStateException("An admin with a similar username already exists");
            }
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setAdmin(isAdmin);
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("User is not logged in. Please log in to continue.");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found. Please log in to continue."));
    }

    public void markProblemSolved(User user, Long problemId) {
        if (user == null) {
            throw new IllegalStateException("User is not logged in. Please log in to solve problems.");
        }

        if (problemId == null) {
            throw new IllegalArgumentException("Problem ID cannot be null");
        }

        // Check if problem exists
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalStateException("Problem not found with id: " + problemId));

        // Check if problem is already solved
        if (user.getSolvedProblems().contains(problemId)) {
            throw new IllegalStateException("You have already solved this problem");
        }

        // Check if admin is trying to solve their own problem
        if (user.isAdmin() && problem.getCreatedBy().equals(user.getId())) {
            throw new IllegalStateException("Admins cannot solve their own problems");
        }

        user.getSolvedProblems().add(problemId);
        userRepository.save(user);
    }

//    public List<Problem> filterProblems(String level, String tags, User user) {
//        if (user == null) {
//            throw new IllegalStateException("User is not logged in. Please log in to view problems.");
//        }
//
//        System.out.println("Filtering problems for user: " + user.getUsername() +
//                ", Level: " + level + ", Tags: " + tags);
//
//        List<Problem> problems = problemRepository.findAll();
//        System.out.println("Total problems in DB: " + problems.size());
//
//        List<Problem> filtered = problems.stream()
//                .filter(p -> !user.getSolvedProblems().contains(p.getId()))
//                .filter(p -> !(user.isAdmin() && p.getCreatedBy().equals(user.getId())))
//                .filter(problem -> (level == null || problem.getLevel().equalsIgnoreCase(level)))
//                .filter(problem -> (tags == null || problem.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tags))))
//                .collect(Collectors.toList());
//
//        filtered.forEach(p -> p.setSolvedByCount(problemRepository.countSolvedBy(p.getId())));
//
//        System.out.println("Filtered problems count: " + filtered.size());
//        return filtered;
//    }
}

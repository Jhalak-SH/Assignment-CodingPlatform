package com.example.codingplatform.controller;

import com.example.codingplatform.model.Problem;
import com.example.codingplatform.model.User;
import com.example.codingplatform.service.ProblemService;
import com.example.codingplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {
    @Autowired
    private ProblemService problemService;

    @Autowired
    private UserService userService;


    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam boolean isAdmin) {
        try {
            User user = userService.createUser(username, email, isAdmin);
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("isAdmin", user.isAdmin());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/users/admins")
    public ResponseEntity<?> getAllAdmins() {
        try {
            List<Map<String, Object>> adminsList = userService.getAllAdmins().stream()
                    .map(admin -> {
                        Map<String, Object> adminMap = new HashMap<>();
                        adminMap.put("id", admin.getId());
                        adminMap.put("username", admin.getUsername());
                        adminMap.put("email", admin.getEmail());
                        adminMap.put("isAdmin", admin.isAdmin());
                        return adminMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("admins", adminsList);
            response.put("count", adminsList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching admins: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/users/regular")
    public ResponseEntity<?> getAllRegularUsers() {
        try {
            List<Map<String, Object>> usersList = userService.getAllUsers().stream()
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", user.getId());
                        userMap.put("username", user.getUsername());
                        userMap.put("email", user.getEmail());
                        userMap.put("isAdmin", user.isAdmin());
                        return userMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("users", usersList);
            response.put("count", usersList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping()
    public ResponseEntity<?> getProblems(@RequestParam String username) {
        User user = userService.findByUsername(username);
        List<Problem> problems = problemService.getUnsolvedProblems(user);

        if (problems.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "message", "You have solved all available problems!",
                    "unsolvedCount", 0
            ));
        }

        return ResponseEntity.ok(Map.of(
                "problems", problems,
                "unsolvedCount", problems.size()
        ));
    }

    @GetMapping("/admin/problems")
    public ResponseEntity<?> getAdminProblems(@RequestParam String username) {
        try {
            User admin = userService.findByUsername(username);
            // Verify if the user is an admin
            if (!admin.isAdmin()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Only admins can access this endpoint");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            List<Problem> adminProblems = problemService.getProblemsByAdmin(admin.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("problems", adminProblems);
            response.put("count", adminProblems.size());
            response.put("adminUsername", admin.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching admin problems: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    @PostMapping
    public ResponseEntity<?> addProblem(
            @RequestBody Problem problem,
            @RequestParam String username) {
        try {
            User admin = userService.findByUsername(username); // Changed to findByUsername
            if (!admin.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only admins can add problems");
            }
            Problem savedProblem = problemService.addProblem(problem, admin);
            return ResponseEntity.ok(savedProblem);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

//    @PostMapping("/mark-solved/{id}")
//    public ResponseEntity<String> markProblemSolved(
//            @PathVariable Long id,
//            @RequestParam String username) {
//        try {
//            User user = userService.findByUsername(username);
//            userService.markProblemSolved(user, id);
//            return ResponseEntity.ok("Problem marked as solved successfully");
//        } catch (IllegalStateException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("Error: " + e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error: " + e.getMessage());
//        }
//    }

    @PostMapping("/mark-solved/{id}")
    public ResponseEntity<?> markProblemSolved(
            @PathVariable Long id,
            @RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            userService.markProblemSolved(user, id);
            return ResponseEntity.ok(Map.of(
                    "message", "Problem marked as solved successfully",
                    "problemId", id
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/solved")
    public ResponseEntity<?> getSolvedProblems(@RequestParam String email) {
        try {
            User user = userService.findByEmail(email);
            List<Problem> problems = problemService.getSolvedProblems(user);
            return ResponseEntity.ok(Map.of(
                    "problems", problems,
                    "solvedCount", problems.size()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateProblem(
            @PathVariable Long id,
            @RequestBody Problem updatedProblem,
            @RequestParam String username) {
        try {
            User admin = userService.findByUsername(username);
            if (!admin.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can update problems"));
            }

            Problem problem = problemService.updateProblem(id, updatedProblem, admin);
            return ResponseEntity.ok(Map.of(
                    "message", "Problem updated successfully",
                    "problem", problem
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error updating problem: " + e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteProblem(
            @PathVariable Long id,
            @RequestParam String username) {
        try {
            User admin = userService.findByUsername(username);
            if (!admin.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can delete problems"));
            }

            problemService.deleteProblem(id, admin);
            return ResponseEntity.ok(Map.of("message", "Problem deleted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting problem: " + e.getMessage()));
        }
    }




    @GetMapping("/filter")
    public ResponseEntity<?> filterProblems(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String tags,
            @RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            List<Problem> problems = problemService.filterProblems(level, tags, user);

            return ResponseEntity.ok(Map.of(
                    "problems", problems,
                    "filteredCount", problems.size(),
                    "level", level != null ? level : "all",
                    "tags", tags != null ? tags : "all"
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}

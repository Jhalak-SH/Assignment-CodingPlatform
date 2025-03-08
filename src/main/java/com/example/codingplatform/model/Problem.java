package com.example.codingplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "problems")
@Data
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ElementCollection
    private List<String> tags;

    private String level;

    private int points;

    @Column(name = "created_by")
    private Long createdBy;

    @Transient
    private long solvedByCount;



    public boolean isValid() {
        return name != null && !name.isEmpty() &&
                description != null && !description.isEmpty() &&
                tags != null && !tags.isEmpty() &&
                level != null &&
                List.of("easy", "medium", "hard").contains(level.toLowerCase()) &&
                points > 0;
    }
}
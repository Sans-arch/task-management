package com.sansarch.task_management.entity;

import jakarta.persistence.*;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(name = "completed", nullable = false)
    private boolean isCompleted;

    @Column(nullable = false)
    private LocalDateTime dueDate;
}

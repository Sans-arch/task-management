package com.github.sansarch.task_management.domain.group.model;

import com.github.sansarch.task_management.domain.group.exception.InvalidGroupStateException;

import java.time.LocalDateTime;
import java.util.Objects;

import static java.util.Objects.isNull;

public class Group {

    private final GroupId id;
    private String name;
    private final LocalDateTime createdAt;

    private Group(GroupId id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;

        this.validate();
    }

    public static Group create(String name) {
        return new Group(GroupId.generate(), name, LocalDateTime.now());
    }

    public static Group reconstitute(GroupId id, String name, LocalDateTime createdAt) {
        return new Group(id, name, createdAt);
    }

    public GroupId getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void validate() {
        if (isNull(id)) {
            throw new InvalidGroupStateException("Group id must not be null");
        }
        if (isNull(name) || name.isBlank()) {
            throw new InvalidGroupStateException("Group name must not be blank");
        }
        if (name.length() > 255) {
            throw new InvalidGroupStateException("Group name must not exceed 255 characters");
        }
        if (isNull(createdAt)) {
            throw new InvalidGroupStateException("Group createdAt must not be null");
        }
    }
}

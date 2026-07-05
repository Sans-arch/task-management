package com.github.sansarch.task_management.infrastructure.group.adapter.in.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddGroupMemberRequest(@NotBlank @Email String memberEmail) {
}

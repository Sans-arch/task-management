package com.github.sansarch.task_management.application.group.dto;

import java.util.UUID;

public record AddGroupMemberCommand(UUID groupId, String memberEmail) {
}

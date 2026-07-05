package com.github.sansarch.task_management.infrastructure.group.adapter.in.web;

import com.github.sansarch.task_management.application.group.dto.AddGroupMemberCommand;
import com.github.sansarch.task_management.application.group.dto.CreateGroupCommand;
import com.github.sansarch.task_management.application.group.dto.RemoveGroupMemberCommand;
import com.github.sansarch.task_management.application.group.port.in.AddGroupMemberUseCase;
import com.github.sansarch.task_management.application.group.port.in.CreateGroupUseCase;
import com.github.sansarch.task_management.application.group.port.in.ListMyGroupsUseCase;
import com.github.sansarch.task_management.application.group.port.in.RemoveGroupMemberUseCase;
import com.github.sansarch.task_management.infrastructure.group.adapter.in.web.request.AddGroupMemberRequest;
import com.github.sansarch.task_management.infrastructure.group.adapter.in.web.request.CreateGroupRequest;
import com.github.sansarch.task_management.infrastructure.group.adapter.in.web.response.GroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Groups", description = "Group and membership operations")
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final AddGroupMemberUseCase addGroupMemberUseCase;
    private final RemoveGroupMemberUseCase removeGroupMemberUseCase;
    private final ListMyGroupsUseCase listMyGroupsUseCase;

    public GroupController(CreateGroupUseCase createGroupUseCase, AddGroupMemberUseCase addGroupMemberUseCase,
                           RemoveGroupMemberUseCase removeGroupMemberUseCase, ListMyGroupsUseCase listMyGroupsUseCase) {
        this.createGroupUseCase = createGroupUseCase;
        this.addGroupMemberUseCase = addGroupMemberUseCase;
        this.removeGroupMemberUseCase = removeGroupMemberUseCase;
        this.listMyGroupsUseCase = listMyGroupsUseCase;
    }

    @Operation(summary = "Create a group", description = "The creator automatically becomes the first member")
    @ApiResponse(responseCode = "201", description = "Group created")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(@RequestBody @Valid CreateGroupRequest request) {
        return GroupResponse.from(createGroupUseCase.create(new CreateGroupCommand(request.name())));
    }

    @Operation(summary = "List the groups the current user belongs to")
    @ApiResponse(responseCode = "200", description = "Groups retrieved")
    @GetMapping
    public List<GroupResponse> listMine() {
        return listMyGroupsUseCase.listMine().stream().map(GroupResponse::from).toList();
    }

    @Operation(summary = "Add a member to a group", description = "The requester must already be a member; repeat calls for an existing member are a no-op")
    @ApiResponse(responseCode = "204", description = "Member added")
    @ApiResponse(responseCode = "403", description = "Requester is not a member of the group")
    @ApiResponse(responseCode = "404", description = "Group not found, or no user with the given email")
    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addMember(@PathVariable UUID id, @RequestBody @Valid AddGroupMemberRequest request) {
        addGroupMemberUseCase.addMember(new AddGroupMemberCommand(id, request.memberEmail()));
    }

    @Operation(summary = "Remove a member from a group", description = "Also used to leave a group by removing yourself; repeat calls for a non-member are a no-op")
    @ApiResponse(responseCode = "204", description = "Member removed")
    @ApiResponse(responseCode = "403", description = "Requester is not a member of the group")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        removeGroupMemberUseCase.removeMember(new RemoveGroupMemberCommand(id, userId));
    }
}

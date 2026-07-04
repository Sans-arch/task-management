package com.github.sansarch.task_management.infrastructure.user.adapter.in.web;

import com.github.sansarch.task_management.application.user.dto.RegisterUserCommand;
import com.github.sansarch.task_management.application.user.port.in.RegisterUserUseCase;
import com.github.sansarch.task_management.infrastructure.user.adapter.in.web.request.RegisterUserRequest;
import com.github.sansarch.task_management.infrastructure.user.adapter.in.web.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "User account operations")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User registered")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody @Valid RegisterUserRequest request) {
        return UserResponse.from(registerUserUseCase.register(new RegisterUserCommand(
                request.email(),
                request.password(),
                request.displayName()
        )));
    }
}

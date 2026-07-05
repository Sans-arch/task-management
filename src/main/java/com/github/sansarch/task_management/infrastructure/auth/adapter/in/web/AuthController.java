package com.github.sansarch.task_management.infrastructure.auth.adapter.in.web;

import com.github.sansarch.task_management.application.auth.dto.LoginCommand;
import com.github.sansarch.task_management.application.auth.port.in.LoginUseCase;
import com.github.sansarch.task_management.infrastructure.auth.adapter.in.web.request.LoginRequest;
import com.github.sansarch.task_management.infrastructure.auth.adapter.in.web.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Authentication operations")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @Operation(summary = "Log in and obtain a JWT")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return LoginResponse.from(loginUseCase.login(new LoginCommand(request.email(), request.password())));
    }
}

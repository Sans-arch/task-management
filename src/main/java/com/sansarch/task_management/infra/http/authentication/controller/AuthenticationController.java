package com.sansarch.task_management.infra.http.authentication.controller;

import com.sansarch.task_management.infra.http.authentication.dto.AuthenticationDTO;
import com.sansarch.task_management.infra.http.authentication.dto.RegistrationDTO;
import com.sansarch.task_management.infra.repository.UserRepository;
import com.sansarch.task_management.infra.repository.model.UserModel;
import jakarta.validation.Valid;
import org.springdoc.core.service.GenericResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GenericResponseService responseBuilder;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO dto) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.login(), dto.password());
        var auth = authenticationManager.authenticate(usernamePassword);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegistrationDTO dto) {
        if (userRepository.findByLogin(dto.login()) != null) {
            return ResponseEntity.badRequest().build();
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(dto.password());
        UserModel newUserModel = new UserModel(dto.login(), encryptedPassword, dto.role());

        userRepository.save(newUserModel);

        return ResponseEntity.ok().build();
    }
}

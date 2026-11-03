package com.example.edutransporte.controller;

import com.example.edutransporte.DTOs.AdminLoginRequest;
import com.example.edutransporte.DTOs.AuthRequest;
import com.example.edutransporte.DTOs.AuthResponse;
import com.example.edutransporte.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Login de aluno/motorista: { "cpf": "...", "senha": "..." } */
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest r) {
        return authService.login(r);
    }

    /** Login do administrador: { "codigo": "..." } — validado no servidor. */
    @PostMapping("/admin")
    public AuthResponse loginAdmin(@RequestBody AdminLoginRequest r) {
        return authService.loginAdmin(r.codigo());
    }
}

package com.example.monitor.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.monitor.dto.AuthResponse;
import com.example.monitor.dto.LoginRequest;
import com.example.monitor.dto.RegisterRequest;
import com.example.monitor.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest
                                           loginRequest) {
        try {
            System.out.println(" Recebendo login request:");
            System.out.println("Email: " + loginRequest.getEmail());
            System.out.println("Senha: " + (loginRequest.getSenha() != null ? "***" :
                    "null"));

            // Validação básica
            if (loginRequest.getEmail() == null ||
                    loginRequest.getEmail().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Email é obrigatório");
                return ResponseEntity.badRequest().body(error);
            }

            if (loginRequest.getSenha() == null ||
                    loginRequest.getSenha().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Senha é obrigatória");
                return ResponseEntity.badRequest().body(error);
            }

            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.out.println(" Erro no login: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.out.println(" Erro inesperado: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro interno do servidor");
            return ResponseEntity.status(500).body(error);
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest
                                              registerRequest) {
        try {
            AuthResponse response =
                    authService.register(registerRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String,
            String> request) {
        try {
            String email = request.get("email");
            authService.forgotPassword(email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email de recuperação enviado comsucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro ao processar solicitação");
            return ResponseEntity.badRequest().body(error);
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String,
            String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            authService.resetPassword(token, newPassword);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Senha redefinida com sucesso");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
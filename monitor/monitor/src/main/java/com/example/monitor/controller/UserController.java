package com.example.monitor.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.monitor.entity.User;
import com.example.monitor.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User userPrincipal) {
        try {
            if (userPrincipal == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Usuário não autenticado");
                return ResponseEntity.status(401).body(error);
            }
            var user = userService.getCurrentUser(userPrincipal.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro ao buscar usuário: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/me/name")
    public ResponseEntity<?> updateUserName(
            @AuthenticationPrincipal User userPrincipal,
            @RequestBody Map<String, String> request) {

        try {
            if (userPrincipal == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Usuário não autenticado");
                return ResponseEntity.status(401).body(error);
            }

            String nome = request.get("nome");
            if (nome == null || nome.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Nome é obrigatório");
                return ResponseEntity.badRequest().body(error);
            }

            var updatedUser = userService.updateUserName(userPrincipal.getEmail(), nome);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updateUserPassword(
            @AuthenticationPrincipal User userPrincipal,
            @RequestBody Map<String, String> request) {
        try {
            if (userPrincipal == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Usuário não autenticado");
                return ResponseEntity.status(401).body(error);
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            userService.updateUserPassword(userPrincipal.getEmail(), currentPassword, newPassword, confirmPassword);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Senha atualizada com sucesso");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(
            @AuthenticationPrincipal User userPrincipal,
            @RequestBody Map<String, String> request) {
        try {
            if (userPrincipal == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Usuário não autenticado");
                return ResponseEntity.status(401).body(error);
            }

            String password = request.get("password");
            if (password == null || password.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Senha é obrigatória para excluir a conta");
                return ResponseEntity.badRequest().body(error);
            }

            userService.deleteUser(userPrincipal.getEmail(), password);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Conta excluída com sucesso");
            System.out.println("Conta excluída: " + userPrincipal.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(409).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro interno do servidor");
            return ResponseEntity.status(500).body(error);
        }
    }
}
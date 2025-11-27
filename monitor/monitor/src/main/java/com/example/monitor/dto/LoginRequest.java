package com.example.monitor.dto;
public class LoginRequest {
    private String email;
    private String senha; // Deve ser "senha" em português

    // Construtores
    public LoginRequest() {}

    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    // Getters e Setters - IMPORTANTE: os nomes devem corresponder ao JSON
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
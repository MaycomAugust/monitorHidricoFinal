package com.example.monitor.dto;

import com.example.monitor.enums.UserRole;


public class UserResponse {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String fotoPerfil;
    private UserRole role;
    public UserResponse() {}
    public UserResponse(Long id, String nome, String email, UserRole role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    public String getFotoPerfil() {
        return fotoPerfil;
    }
    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }


}
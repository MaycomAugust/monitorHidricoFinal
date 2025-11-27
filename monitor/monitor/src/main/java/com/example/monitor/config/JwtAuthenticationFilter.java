package com.example.monitor.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.monitor.entity.User;
import com.example.monitor.repository.UserRepository;
import com.example.monitor.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Permitir preflight sem validação
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Se endpoints públicos (ex: sensors), pode pular se quiser:
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/sensors")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // não há token: deixa o filtro seguir; endpoints protegidos receberão 401/403 pelo Spring
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtService.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.getEmailFromToken(token);
            if (email == null) {
                filterChain.doFilter(request, response);
                return;
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            var authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // log opcional
            System.out.println("   Usuário autenticado (JWT): " + email);

        } catch (Exception e) {
            System.out.println("   Falha ao validar token JWT: " + e.getMessage());
            // segue em frente sem autenticação
        }

        filterChain.doFilter(request, response);
    }
}
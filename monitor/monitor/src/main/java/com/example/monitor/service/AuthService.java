package com.example.monitor.service;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.monitor.dto.AuthResponse;
import com.example.monitor.dto.LoginRequest;
import com.example.monitor.dto.RegisterRequest;
import com.example.monitor.dto.UserResponse;
import com.example.monitor.entity.PasswordResetToken;
import com.example.monitor.entity.User;
import com.example.monitor.repository.PasswordResetTokenRepository;
import com.example.monitor.repository.UserRepository;
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private EmailService emailService;
    public AuthResponse login(LoginRequest loginRequest) {
        Optional<User> userOptional =
                userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Usuario não encontrado");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getSenha(),
                user.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        if (!user.isAtivo()) {
            throw new RuntimeException("Usuário inativo");
        }

        String token = jwtService.generateToken(user.getEmail());

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole());

        userResponse.setTelefone(user.getTelefone());
        userResponse.setFotoPerfil(user.getFotoPerfil());

        return new AuthResponse(token, userResponse);
    }
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = new User();
        user.setNome(registerRequest.getNome());
        user.setEmail(registerRequest.getEmail());
        user.setSenha(passwordEncoder.encode(registerRequest.getSenha()));
        user.setTelefone(registerRequest.getTelefone());
        user.setAtivo(true); // GARANTIR que novos usuários sejam ativos

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail());

        UserResponse userResponse = new UserResponse(
                savedUser.getId(),
                savedUser.getNome(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
        userResponse.setTelefone(savedUser.getTelefone());

        return new AuthResponse(token, userResponse);
    }
    public void forgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return;
        }
        User user = userOptional.get();
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);

        // Enviar email
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOptional =
                passwordResetTokenRepository.findByToken(token);

        if (resetTokenOptional.isEmpty()) {
            throw new RuntimeException("Token inválido");
        }

        PasswordResetToken resetToken = resetTokenOptional.get();

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token expirado");
        }

        if (resetToken.isUsado()) {
            throw new RuntimeException("Token já utilizado");
        }

        User user = resetToken.getUser();
        user.setSenha(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsado(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
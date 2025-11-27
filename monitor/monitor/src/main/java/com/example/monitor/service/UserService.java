
package com.example.monitor.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.monitor.dto.UserResponse;
import com.example.monitor.entity.User;
import com.example.monitor.repository.FcmTokenRepository;
import com.example.monitor.repository.NotificationRepository;
import com.example.monitor.repository.PasswordResetTokenRepository;
import com.example.monitor.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public UserResponse getCurrentUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Usuario nao encontrado");
        }

        User user = userOptional.get();
        return convertToUserResponse(user);
    }

    public UserResponse updateUserName(String email, String nome) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        user.setNome(nome);
        User updatedUser = userRepository.save(user);
        System.out.println("  [ ]  Nome atualizado para: " + nome);
        return convertToUserResponse(updatedUser);
    }

    public void updateUserPassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getSenha())) {
            throw new RuntimeException("Senha atual incorreta");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Nova senha e confirmação não coincidem");
        }

        if (passwordEncoder.matches(newPassword, user.getSenha())) {
            throw new RuntimeException("Nova senha deve ser diferente da senha atual");
        }

        user.setSenha(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        System.out.println(" 😊 Senha atualizada para usuário: " + email);
    }

    public void deleteUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(password, user.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        fcmTokenRepository.deleteByUser(user);
        notificationRepository.deleteByUser(user);
        passwordResetTokenRepository.deleteByUser(user);
        userRepository.delete(user);

        System.out.println(" ✔ Usuário e dados relacionados excluídos: " + email);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setNome(user.getNome());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setTelefone(user.getTelefone());
        response.setFotoPerfil(user.getFotoPerfil());
        return response;
    }
}

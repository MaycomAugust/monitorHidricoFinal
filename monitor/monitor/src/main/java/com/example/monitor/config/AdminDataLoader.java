package com.example.monitor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.example.monitor.entity.User;
import com.example.monitor.enums.UserRole;
import com.example.monitor.repository.UserRepository;

@Component
public class AdminDataLoader implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("adm123@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setNome("Administrador");
            admin.setEmail("adm123@gmail.com");
            admin.setSenha(passwordEncoder.encode("741852963"));
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setAtivo(true);

            userRepository.save(admin);
            System.out.println("   Usuário criado com sucesso!");
        } else {
            System.out.println("    Usuário admin já existe");
        }
    }
}
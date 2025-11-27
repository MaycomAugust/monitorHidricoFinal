package com.example.monitor.repository;
import com.example.monitor.entity.PasswordResetToken;
import com.example.monitor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Repository
public interface PasswordResetTokenRepository extends
        JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserIdAndUsadoFalse(Long userId);

    // ADICIONE ESTE MÉTODO
    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user = :user")
    void deleteByUser(User user);
}
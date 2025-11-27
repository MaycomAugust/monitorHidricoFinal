package com.example.monitor.repository;
import com.example.monitor.entity.FcmToken;
import com.example.monitor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    List<FcmToken> findByUser(User user);

    List<FcmToken> findByUserAndActiveTrue(User user);

    List<FcmToken> findByActiveTrue();

    @Transactional
    @Modifying
    @Query("UPDATE FcmToken f SET f.active = false WHERE f.user = :user")
    void deactivateAllByUser(@Param("user") User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM FcmToken f WHERE f.user = :user")
    void deleteByUser(@Param("user") User user);
}
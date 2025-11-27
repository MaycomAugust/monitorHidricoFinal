package com.example.monitor.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.monitor.dto.FcmTokenRequest;
import com.example.monitor.entity.Notification;
import com.example.monitor.entity.User;
import com.example.monitor.repository.NotificationRepository;
import com.example.monitor.service.NotificationService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService = new NotificationService();
    private final NotificationRepository notificationRepository = null;
    private static final Logger log =
            LoggerFactory.getLogger(NotificationController.class);
    @PostMapping("/register-token")
    public ResponseEntity<?> registerFcmToken(
            @AuthenticationPrincipal User user,
            @RequestBody FcmTokenRequest request) {

        try {
            String token = request.getToken();
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Token éobrigatório"));
            }
            notificationService.registerFcmToken(user, token);

            log.info("Token FCM registrado para usuário: {}", user.getEmail());
            return ResponseEntity.ok(createSuccessResponse("Token registrado com sucesso"));

        } catch (Exception e) {
            log.error("Erro ao registrar token FCM: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Erro ao registrar token"));
        }
    }
    @GetMapping("/my-notifications")
    public ResponseEntity<?> getUserNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        try {
            List<Notification> notifications;
            if (unreadOnly) {
                notifications =
                        notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
            } else {
                notifications =
                        notificationRepository.findByUserOrderByCreatedAtDesc(user);
            }

            // Converter para DTO
            List<Map<String, Object>> response = notifications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao buscar notificações: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Erro ao buscar notificações"));
        }
    }
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User user)
    {
        try {
            long count = notificationRepository.countByUserAndReadFalse(user);
            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao contar notificações não lidas: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Erro ao contar notificações"));
        }
    }
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        try {
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

            // Verificar se a notificação pertence ao usuário
            if (!notification.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(createErrorResponse("Acesso negado"));
            }

            notification.setRead(true);
            notificationRepository.save(notification);

            return ResponseEntity.ok(createSuccessResponse("Notificação marcada como lida"));

        } catch (Exception e) {
            log.error("Erro ao marcar notificação como lida: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Erro ao marcar como lida"));
        }
    }
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal User user) {
        try {
            int updated = notificationRepository.markAllAsReadByUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Todas as notificações marcadas como lidas");
            response.put("updatedCount", updated);

            log.info("Marcadas {} notificações como lidas para usuário: {}", updated,
                    user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao marcar todas como lidas: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Erro ao marcar como lidas"));
        }
    }
    @PostMapping("/test")
    public ResponseEntity<?> sendTestNotification(@AuthenticationPrincipal User
                                                          user) {
        try {
            notificationService.sendSystemNotification(
                    user,
                    "Teste de Notificação",
                    "Esta é uma notificação de teste do sistema MonitorHídrico"
            );

            return ResponseEntity.ok(createSuccessResponse("Notificação de teste enviada"));

        } catch (Exception e) {
            log.error("Erro ao enviar notificação de teste: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Erro ao enviar teste"));
        }
    }
    // Métodos auxiliares
    private Map<String, Object> convertToResponse(Notification notification) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", notification.getId());
        response.put("title", notification.getTitle());
        response.put("message", notification.getMessage());
        response.put("type", notification.getType());
        response.put("read", notification.isRead());
        response.put("sensorId", notification.getSensorId());
        response.put("createdAt", notification.getCreatedAt());
        return response;
    }
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
}
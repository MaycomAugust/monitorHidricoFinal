package com.example.monitor.service;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.monitor.entity.FcmToken;
import com.example.monitor.entity.Notification;
import com.example.monitor.entity.User;
import com.example.monitor.repository.FcmTokenRepository;
import com.example.monitor.repository.NotificationRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final FirebaseMessaging firebaseMessaging = null;
    private final FcmTokenRepository fcmTokenRepository = null;
    private final NotificationRepository notificationRepository = null;
    private static final Logger log =
            LoggerFactory.getLogger(NotificationService.class);

    public void sendNotificationToUser(User user, String title, String message, String
            type, String sensorId) {
        try {
            // Salvar notificação no banco
            Notification notification = new Notification(user, title, message, type,
                    sensorId);
            notificationRepository.save(notification);
            // Enviar notificação via FCM
            List<FcmToken> userTokens =
                    fcmTokenRepository.findByUserAndActiveTrue(user);

            if (!userTokens.isEmpty()) {
                List<String> tokens = userTokens.stream()
                        .map(FcmToken::getToken)
                        .collect(Collectors.toList());
                MulticastMessage multicastMessage = MulticastMessage.builder()
                        .setNotification(com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(message)
                                .build())
                        .putData("type", type)
                        .putData("sensorId", sensorId != null ? sensorId : "")
                        .putData("notificationId", notification.getId().toString())
                        .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                        .addAllTokens(tokens)
                        .build();
                BatchResponse response =
                        firebaseMessaging.sendEachForMulticast(multicastMessage);
                log.info("Notificações enviadas para usuário {}: {}/{} sucessos",
                        user.getEmail(), response.getSuccessCount(), tokens.size());

                // Lidar com tokens inválidos
                if (response.getFailureCount() > 0) {
                    handleFailedTokens(response, tokens);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao enviar notificação para usuário {}: {}", user.getEmail(),
                    e.getMessage());
        }
    }
    public void sendNotificationToAllUsers(String title, String message, String type) {
        try {
            List<FcmToken> allActiveTokens = fcmTokenRepository.findByActiveTrue();

            // Agrupar por usuário para evitar notificações duplicadas
            allActiveTokens.stream()
                    .collect(Collectors.groupingBy(FcmToken::getUser))
                    .forEach((user, tokens) -> {
                        // Salvar notificação para cada usuário
                        Notification notification = new Notification(user, title, message, type);
                        notificationRepository.save(notification);
                    });
            // Enviar para todos os tokens ativos
            if (!allActiveTokens.isEmpty()) {
                List<String> tokens = allActiveTokens.stream()
                        .map(FcmToken::getToken)
                        .collect(Collectors.toList());
                MulticastMessage multicastMessage = MulticastMessage.builder()
                        .setNotification(com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(message)
                                .build())
                        .putData("type", type)
                        .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                        .addAllTokens(tokens)
                        .build();
                BatchResponse response =
                        firebaseMessaging.sendEachForMulticast(multicastMessage);
                log.info("Notificação global enviada: {}/{} sucessos",
                        response.getSuccessCount(), tokens.size());

                if (response.getFailureCount() > 0) {
                    handleFailedTokens(response, tokens);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao enviar notificação global: {}", e.getMessage());
        }
    }
    public void registerFcmToken(User user, String token) {
        try {
            // Verificar se token já existe
            fcmTokenRepository.findByToken(token)
                    .ifPresentOrElse(
                            existingToken -> {
                                // Atualizar usuário se necessário e reativar token
                                if (!existingToken.getUser().getId().equals(user.getId())) {
                                    existingToken.setUser(user);
                                }
                                existingToken.setActive(true);
                                fcmTokenRepository.save(existingToken);
                                log.info("Token FCM atualizado para usuário: {}", user.getEmail());
                            },
                            () -> {
                                // Criar novo token
                                FcmToken newToken = new FcmToken(user, token);
                                fcmTokenRepository.save(newToken);
                                log.info("Novo token FCM registrado para usuário: {}",
                                        user.getEmail());
                            }
                    );
        } catch (Exception e) {
            log.error("Erro ao registrar token FCM para usuário {}: {}", user.getEmail(),
                    e.getMessage());
        }
    }
    private void handleFailedTokens(BatchResponse response, List<String> tokens) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                String failedToken = tokens.get(i);
                // Desativar token inválido
                fcmTokenRepository.findByToken(failedToken).ifPresent(token -> {
                    token.setActive(false);
                    fcmTokenRepository.save(token);
                    log.warn("Token FCM desativado por invalidez: {}",
                            failedToken.substring(0, 20) + "...");
                });
            }
        }
    }


    public void sendSensorAlert(User user, String sensorName, double nivel, String
            status) {
        String title = "";
        String message = "";

        switch (status) {
            case "CRITICO":
                title = " ALERTA CRÍTICO - " + sensorName;
                message = String.format("Nível CRÍTICO: %.2fm. Ação imediata necessária!", nivel);
                break;
            case "ATENCAO":
                title = " ALERTA DE ATENÇÃO - " + sensorName;
                message = String.format("Nível elevado: %.2fm. Monitorar situação.",
                        nivel);
                break;
            default:
                title = " Alerta do Sensor - " + sensorName;
                message = String.format("Nível: %.2fm - Status: %s", nivel, status);
        }

        sendNotificationToUser(user, title, message, status, sensorName);
    }
    public void sendSystemNotification(User user, String title, String message) {
        sendNotificationToUser(user, title, message, "SISTEMA", null);
    }
}
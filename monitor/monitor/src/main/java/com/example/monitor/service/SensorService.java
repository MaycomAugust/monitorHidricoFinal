
package com.example.monitor.service;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.monitor.entity.Sensor;
import com.example.monitor.entity.User;
import com.example.monitor.repository.SensorRepository;
import com.example.monitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class SensorService {
    private final NotificationService notificationService = new NotificationService();
    private final UserRepository userRepository = null;
    @Autowired
    private SensorRepository sensorRepository; // GARANTE QUE NÃO É NULL
    public Optional<Sensor> buscarPorId(Long id) {
        return sensorRepository.findById(id);
    }
    public Sensor salvar(Sensor sensor) {
        return sensorRepository.save(sensor);
    }
    public void deletar(Long id) {
        sensorRepository.deleteById(id);
    }
    private static final Logger log = LoggerFactory.getLogger(SensorService.class);
    public List<Sensor> listarSensores() {
        return sensorRepository.findAll();
    }

    public Sensor ativarOuDesativar(Long id, boolean ativo) {
        Optional<Sensor> sensorOpt = sensorRepository.findById(id);
        if (sensorOpt.isPresent()) {
            Sensor sensor = sensorOpt.get();
            sensor.setAtivo(ativo);
            return sensorRepository.save(sensor);
        }
        return null;
    }

    public Sensor atualizarStatus(Long id, boolean ativo) {
        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sensor não encontrado"));
        sensor.setAtivo(ativo);
        return sensorRepository.save(sensor);
    }

    public void handleSensorData(double nivelAgua, String sensorId, String
            sensorNome) {
        log.info(" Processando dados do sensor: {} - Nível: {}m", sensorNome,
                nivelAgua);

        // Garante que o sensor exista
        Sensor sensor = sensorRepository.findBySensorId(sensorId)
                .orElseGet(() -> {
                    Sensor novo = new Sensor();
                    novo.setSensorId(sensorId);
                    novo.setNome(sensorNome);
                    novo.setAtivo(true);
                    return sensorRepository.save(novo);
                });

        if (!sensor.isAtivo()) {
            log.warn(" Sensor {} está desativado. Ignorando dados.",
                    sensor.getNome());
            return;
        }

        //Atualiza status com base no nível
        if (nivelAgua >= 4.0) {
            sensor.setStatus("CRITICO");
            enviarAlertaCritico(nivelAgua, sensorId, sensorNome);
        } else if (nivelAgua >= 3.5) {
            sensor.setStatus("ATENCAO");
            enviarAlertaAtencao(nivelAgua, sensorId, sensorNome);
        } else {
            sensor.setStatus("NORMAL");
        }

        sensorRepository.save(sensor);
    }

    private void enviarAlertaCritico(double nivelAgua, String sensorId, String
            sensorNome) {
        String titulo = " ALERTA CRÍTICO - " + sensorNome;
        String mensagem = String.format("Nível CRÍTICO detectado: %.2fm no sensor %s. Ação imediata necessária!",
                nivelAgua, sensorNome);

        // Enviar para TODOS os usuários logados/ativos
        List<User> usuariosAtivos = userRepository.findByAtivoTrue();

        log.info(" Enviando alerta crítico para {} usuários", usuariosAtivos.size());

        for (User usuario : usuariosAtivos) {
            notificationService.sendSensorAlert(usuario, sensorNome, nivelAgua,
                    "CRITICO");
        }
    }
    private void enviarAlertaAtencao(double nivelAgua, String sensorId, String
            sensorNome) {
        String titulo = " ALERTA DE ATENÇÃO - " + sensorNome;
        String mensagem = String.format("Nível elevado: %.2fm no sensor %s. Monitorar situação.",
                nivelAgua, sensorNome);

        // Enviar para todos os usuários ativos
        List<User> usuariosAtivos = userRepository.findByAtivoTrue();

        for (User usuario : usuariosAtivos) {
            notificationService.sendSensorAlert(usuario, sensorNome, nivelAgua, "ATENCAO");
        }
    }
}

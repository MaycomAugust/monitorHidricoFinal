package com.example.monitor.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.monitor.entity.Sensor;
import com.example.monitor.service.SensorService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class SensorController {
    @Autowired
    private SensorService sensorService;


    private static final Logger log = LoggerFactory.getLogger(SensorController.class);
    @PostMapping("/{sensorId}/data")
    public ResponseEntity<?> receiveSensorData(
            @PathVariable String sensorId,
            @RequestParam double nivel,
            @RequestParam double fluxo,
            @RequestParam String status) {

        try {
            log.info(" Recebendo dados do sensor {}: Nível={}m, Fluxo={}, Status={}",
                    sensorId, nivel, fluxo, status);

            // Processar dados do sensor (nome pode ser o ID ou você pode mapear para um nome amigável)
            String sensorNome = "Sensor " + sensorId.substring(0, 8) + "...";

            sensorService.handleSensorData(nivel, sensorId, sensorNome);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Dados do sensor processados com sucesso");
            response.put("sensor", sensorId);
            response.put("nivel", String.valueOf(nivel));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error(" Erro ao processar dados do sensor: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao processar dados do sensor");
            return ResponseEntity.badRequest().body(error);
        }
    }



    @PostMapping("/{id}/ativo")
    public ResponseEntity<Sensor> atualizarAtivo(@PathVariable Long id, @RequestBody Map<String, Boolean> body ) {
        boolean ativo = body.get("ativo");
        return ResponseEntity.ok(sensorService.atualizarStatus(id, ativo));
    }

    // Listar todos os sensores
    @GetMapping
    public List<Sensor> listarSensores() {
        return sensorService.listarSensores();
    }
    // Ativar ou desativar sensor
    @PutMapping("/{id}/status")
    public Sensor alterarStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        return sensorService.ativarOuDesativar(id, ativo);
    }
    // Excluir sensor
    @DeleteMapping("/{id}")
    public void excluirSensor(@PathVariable Long id) {
        sensorService.deletar(id);
    }

    @PostMapping
    public ResponseEntity<Sensor> criarSensor(@RequestBody Sensor sensor) {
        Sensor novoSensor = sensorService.salvar(sensor);
        return ResponseEntity.ok(novoSensor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSensor(@PathVariable Long id, @RequestBody Sensor sensorAtualizado) {
        try {
            // Buscar o sensor existente
            Optional<Sensor> sensorExistenteOpt = sensorService.buscarPorId(id);

            if (sensorExistenteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Sensor não encontrado com ID: " + id);
            }

            Sensor sensorExistente = sensorExistenteOpt.get();

            // Atualizar apenas os campos permitidos
            sensorExistente.setNome(sensorAtualizado.getNome());
            sensorExistente.setLocalicao(sensorAtualizado.getLocalicao());
            sensorExistente.setLatitude(sensorAtualizado.getLatitude());
            sensorExistente.setLongitude(sensorAtualizado.getLongitude());
            sensorExistente.setStatus(sensorAtualizado.getStatus());

            // O campo 'ativo' não é atualizado aqui para manter o estado atual
            // O campo 'sensorId' também não é alterado

            // Salvar as alterações
            Sensor sensorAtualizadoSalvo = sensorService.salvar(sensorExistente);

            return ResponseEntity.ok(sensorAtualizadoSalvo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar sensor: " + e.getMessage());
        }

    }
}
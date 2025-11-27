package com.example.monitor.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "sensores")
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String sensorId;
    private String nome;
    private String localicao;
    private double latitude;
    private double longitude;
    private boolean ativo;
    private String status = "NORMAL"; //NORMAL, ATENCAO, ALERTA, CRITICO
    public Sensor(Long id, String nome, String localicao, double latitude,
                  double longitude, boolean ativo, String status) {
        super();
        this.id = id;
        this.nome = nome;
        this.localicao = localicao;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ativo = ativo;
        this.status = status;
    }
    public Sensor() {
// TODO Auto-generated constructor stub
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getLocalicao() {
        return localicao;
    }
    public void setLocalicao(String localicao) {
        this.localicao = localicao;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public boolean isAtivo() {
        return ativo;
    }
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getSensorId() {
        return sensorId;
    }
    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
}
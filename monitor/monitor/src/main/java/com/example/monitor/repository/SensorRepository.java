package com.example.monitor.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.monitor.entity.Sensor;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long>{
    Optional<Sensor> findBySensorId(String sensorId);
}





//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;

//import java.util.UUID;

//@Repository
//public interface SensorRepository extends JpaRepository<Sensor, UUID> {
//    boolean existsBySensorId(UUID sensorId);
//}
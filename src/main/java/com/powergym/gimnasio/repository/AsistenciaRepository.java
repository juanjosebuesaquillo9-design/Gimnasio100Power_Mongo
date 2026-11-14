package com.powergym.gimnasio.repository;

import com.powergym.gimnasio.model.Asistencia;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AsistenciaRepository extends MongoRepository<Asistencia, String> {
    List<Asistencia> findTop50ByOrderByFechaHoraDesc();
    long countByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
}
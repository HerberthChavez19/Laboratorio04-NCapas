package com.server.app.repositories;

import com.server.app.entities.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.usuario.id = :usuarioId " +
           "AND (:fechaDesde IS NULL OR m.fecha >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR m.fecha <= :fechaHasta)")
    Page<Movimiento> findByUsuarioIdAndFechas(
            @Param("usuarioId") int usuarioId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable);
}

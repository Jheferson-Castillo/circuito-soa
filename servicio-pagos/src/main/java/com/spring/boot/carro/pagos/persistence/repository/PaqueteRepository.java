package com.spring.boot.carro.pagos.persistence.repository;

import com.spring.boot.carro.pagos.persistence.entity.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    List<Paquete> findByActivoTrue();

    @Query("SELECT COUNT(p) > 0 FROM Pago p WHERE p.paquete.id = :paqueteId")
    boolean existsByPaqueteIdInPagos(@Param("paqueteId") Long paqueteId);
}

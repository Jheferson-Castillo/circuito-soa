package com.spring.boot.carro.pagos.persistence.repository;

import com.spring.boot.carro.pagos.persistence.entity.DetallePago;
import com.spring.boot.carro.pagos.persistence.entity.DetallePagoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallePagoRepository extends JpaRepository<DetallePago, DetallePagoId> {

}

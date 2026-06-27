package com.spring.boot.carro.reservas.persistence.repository;

import com.spring.boot.carro.reservas.persistence.entity.Vehiculo;
import com.spring.boot.carro.reservas.persistence.enums.EstadoVehiculosEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    List<Vehiculo> findByActivoTrueAndEstado(EstadoVehiculosEnum estado);

    boolean existsByPlaca(String placa);

    @Modifying
    @Query("UPDATE Vehiculo v SET v.estado = :estado WHERE v.id = :id")
    void actualizarEstadoVehiculo(Long id, EstadoVehiculosEnum estado);

    List<Vehiculo> findByActivoTrueAndEstadoNot(EstadoVehiculosEnum estado);

    List<Vehiculo> findByActivoTrue();
}

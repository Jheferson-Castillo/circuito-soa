package com.spring.boot.carro.usuarios.persistence.repository;

import com.spring.boot.carro.usuarios.persistence.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    List<Usuario> findByActivoTrue();
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
    Optional<Usuario> findByEmail(String email);

}

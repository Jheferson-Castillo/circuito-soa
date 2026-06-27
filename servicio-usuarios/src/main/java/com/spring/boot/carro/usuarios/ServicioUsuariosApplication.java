package com.spring.boot.carro.usuarios;

import com.spring.boot.carro.usuarios.persistence.entity.Usuario;
import com.spring.boot.carro.usuarios.persistence.enums.RolEnum;
import com.spring.boot.carro.usuarios.persistence.enums.TipoDocumentoEnum;
import com.spring.boot.carro.usuarios.persistence.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class ServicioUsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioUsuariosApplication.class, args);
    }

    // Carga usuarios de prueba SOLO si la tabla esta vacia, con contrasena BCrypt y rol,
    // para poder probar el login (POST /api/v1/usuarios/auth/login).
    @Bean
    CommandLineRunner init(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() > 0) {
                return;
            }
            List<Usuario> usuarios = List.of(
                    // ADMIN  | login: admin@circuito.com / admin123
                    Usuario.builder()
                            .nombre("Italo Jeampierre").apellido("Carlos Roque")
                            .email("admin@circuito.com")
                            .tipoDocumento(TipoDocumentoEnum.DNI).numeroDocumento("70757085")
                            .telefono("970818053")
                            .fechaRegistro(LocalDateTime.now()).activo(true)
                            .saldoMinutos(0)
                            .rol(RolEnum.ADMIN)
                            .password(passwordEncoder.encode("admin123"))
                            .build(),
                    // INSTRUCTOR | login: instructor@circuito.com / instructor123
                    Usuario.builder()
                            .nombre("Carlos").apellido("Vilca Mendoza")
                            .email("instructor@circuito.com")
                            .tipoDocumento(TipoDocumentoEnum.DNI).numeroDocumento("16132076")
                            .telefono("994205861")
                            .fechaRegistro(LocalDateTime.now()).activo(true)
                            .saldoMinutos(0)
                            .rol(RolEnum.INSTRUCTOR)
                            .password(passwordEncoder.encode("instructor123"))
                            .build(),
                    // ALUMNO | login: alumno@circuito.com / alumno123
                    Usuario.builder()
                            .nombre("Maria").apellido("Perez Gomez")
                            .email("alumno@circuito.com")
                            .tipoDocumento(TipoDocumentoEnum.DNI).numeroDocumento("45612378")
                            .telefono("987654321")
                            .fechaRegistro(LocalDateTime.now()).activo(true)
                            .saldoMinutos(0)
                            .rol(RolEnum.ALUMNO)
                            .password(passwordEncoder.encode("alumno123"))
                            .build()
            );
            usuarioRepository.saveAll(usuarios);
        };
    }
}

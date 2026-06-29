package com.spring.boot.carro.usuarios.presentation.dto.usuario;

import com.spring.boot.carro.usuarios.persistence.enums.RolEnum;
import com.spring.boot.carro.usuarios.persistence.enums.TipoDocumentoEnum;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequestDTO {


    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;


    @Size(max = 50, message = "El apellido no puede exceder los 50 caracteres")
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumentoEnum tipoDocumento;


    @Size(max = 20, message = "El numero documento no puede exceder los 20 caracteres")
    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9\\s()-]{7,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    @Email(message = "Formato de correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    // Rol del usuario. Opcional: si no se envía, el servicio asigna ALUMNO por defecto.
    private RolEnum rol;

    // Contraseña en claro (se encripta con BCrypt en el servicio).
    // Obligatoria al CREAR (se valida en UsuarioService.crear). En la actualización (PUT)
    // puede omitirse para conservar la actual; si se envía, debe cumplir el mínimo de longitud.
    @Size(min = 8, max = 72, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

}

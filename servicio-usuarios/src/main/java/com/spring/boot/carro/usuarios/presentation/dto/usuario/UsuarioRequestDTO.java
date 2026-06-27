package com.spring.boot.carro.usuarios.presentation.dto.usuario;

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

}

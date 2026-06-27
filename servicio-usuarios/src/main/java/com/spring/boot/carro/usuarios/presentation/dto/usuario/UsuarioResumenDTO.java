package com.spring.boot.carro.usuarios.presentation.dto.usuario;

import com.spring.boot.carro.usuarios.persistence.enums.TipoDocumentoEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UsuarioResumenDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private TipoDocumentoEnum tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String email;
}

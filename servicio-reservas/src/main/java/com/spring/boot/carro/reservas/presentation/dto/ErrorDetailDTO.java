package com.spring.boot.carro.reservas.presentation.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ErrorDetailDTO {
    private String mensaje;
    private String codigo;
    private String path;
    private LocalDateTime timeStamp;
    private List<String> detalles;

    public ErrorDetailDTO(String mensaje, String codigo, String path, List<String> detalles) {
        this.mensaje = mensaje;
        this.codigo = codigo;
        this.path = path;
        this.detalles = detalles;
        this.timeStamp = LocalDateTime.now();
    }
}

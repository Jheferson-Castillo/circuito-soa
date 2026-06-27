package com.spring.boot.carro.usuarios.persistence.enums;

// Un rol por usuario. Se incluye en los claims del JWT (claim "rol").
public enum RolEnum {
    ADMIN,
    INSTRUCTOR,
    ALUMNO
}

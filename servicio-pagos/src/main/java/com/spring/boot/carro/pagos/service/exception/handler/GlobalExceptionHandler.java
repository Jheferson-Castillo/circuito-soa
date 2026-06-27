package com.spring.boot.carro.pagos.service.exception.handler;

import com.spring.boot.carro.pagos.presentation.dto.ErrorDetailDTO;
import com.spring.boot.carro.pagos.service.exception.BusinessException;
import com.spring.boot.carro.pagos.service.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Validaciones con @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetailDTO> mensajeErroresDeValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorDetailDTO errorDetailDTO = new ErrorDetailDTO(
                "Error de validacion",
                "VALIDATION_ERROR",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.badRequest().body(errorDetailDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetailDTO> handleGeneral(Exception ex,HttpServletRequest request){
        ErrorDetailDTO errorDetailDTO=new ErrorDetailDTO(
                ex.getMessage(),
                "OCURRIO_UN_ERROR_INESPERADO - INTERNAL_ERROR",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.badRequest().body(errorDetailDTO);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDetailDTO> handleBusiness(BusinessException ex, HttpServletRequest request) {

        ErrorDetailDTO errorDetailDTO = new ErrorDetailDTO(
                ex.getMessage(),
                "LOGICA_DE_NEGOCIO - BUSINESS_ERROR",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.badRequest().body(errorDetailDTO);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDetailDTO> handleNotFoynd(NotFoundException ex, HttpServletRequest request){

        ErrorDetailDTO errorDetailDTO=new ErrorDetailDTO(
                ex.getMessage(),
                "NOT_FOUND_ERROR ",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.badRequest().body(errorDetailDTO);
    }
}

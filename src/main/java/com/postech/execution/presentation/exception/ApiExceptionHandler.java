package com.postech.execution.presentation.exception;

import com.postech.execution.domain.exception.ExecucaoJaExisteException;
import com.postech.execution.domain.exception.ExecucaoNaoEncontradaException;
import com.postech.execution.domain.exception.TransicaoStatusInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ExecucaoNaoEncontradaException.class)
    public ProblemDetail handleNotFound(ExecucaoNaoEncontradaException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ExecucaoJaExisteException.class, TransicaoStatusInvalidaException.class, IllegalArgumentException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
}

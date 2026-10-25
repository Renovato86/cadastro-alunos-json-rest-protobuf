package br.edu.atividade.alunos;

import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record Erro(int status, String mensagem) {}

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Erro> recurso(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new Erro(ex.getStatusCode().value(), ex.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> validacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage()).sorted().distinct()
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new Erro(400, mensagem));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Erro> formatoInvalido(Exception ex) {
        return ResponseEntity.badRequest().body(new Erro(400, "JSON ou identificador inválido"));
    }
}

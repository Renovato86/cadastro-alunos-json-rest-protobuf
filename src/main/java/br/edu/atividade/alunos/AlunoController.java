package br.edu.atividade.alunos;

import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/alunos", produces = MediaType.APPLICATION_JSON_VALUE)
public class AlunoController {
    private final AlunoRepository repository;

    public AlunoController(AlunoRepository repository) {
        this.repository = repository;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Aluno> cadastrar(@Valid @RequestBody Aluno aluno) {
        if (!repository.cadastrar(aluno)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um aluno com esse id");
        }
        return ResponseEntity.created(URI.create("/alunos/" + aluno.id())).body(aluno);
    }

    @GetMapping("/{id}")
    public Aluno consultar(@PathVariable int id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id deve ser positivo");
        }
        return repository.buscar(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
    }
}

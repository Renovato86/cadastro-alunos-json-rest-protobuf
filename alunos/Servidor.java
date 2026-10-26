package alunos;

import java.util.Map;
import java.util.List;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class Servidor {
    private final Map<Integer, Aluno> alunos = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(Servidor.class, args);
    }

    @PostMapping("/alunos")
    public ResponseEntity<Aluno> cadastrar(@RequestBody Aluno aluno) {
        if (aluno == null || aluno.id <= 0 || aluno.nome == null || aluno.nome.isBlank()
                || aluno.curso == null || aluno.curso.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        aluno.nome = aluno.nome.trim();
        aluno.curso = aluno.curso.trim();
        boolean novo = alunos.put(aluno.id, aluno) == null;
        return ResponseEntity.status(novo ? 201 : 200).body(aluno);
    }

    @GetMapping("/alunos")
    public List<Aluno> listar() {
        return alunos.values().stream()
                .sorted(Comparator.comparingInt(aluno -> aluno.id))
                .toList();
    }

    @GetMapping("/alunos/{id}")
    public ResponseEntity<Aluno> consultar(@PathVariable int id) {
        Aluno aluno = alunos.get(id);
        if (aluno == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(aluno);
    }
}

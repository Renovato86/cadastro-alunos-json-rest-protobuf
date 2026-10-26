import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alunos")
public class AlunoController {
    private final Map<Integer, Aluno> alunos = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<Aluno> cadastrar(@RequestBody Aluno aluno) {
        if (alunos.putIfAbsent(aluno.getId(), aluno) != null) {
            return ResponseEntity.status(409).build();
        }
        return ResponseEntity.created(URI.create("/alunos/" + aluno.getId())).body(aluno);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aluno> consultar(@PathVariable int id) {
        Aluno aluno = alunos.get(id);
        if (aluno == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(aluno);
    }
}

package br.edu.atividade.alunos;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class AlunoRepository {
    // Armazenamento didático: os dados são perdidos ao reiniciar o servidor.
    private final ConcurrentMap<Integer, Aluno> alunos = new ConcurrentHashMap<>();

    public boolean cadastrar(Aluno aluno) {
        // Operação atômica: duas requisições não sobrescrevem o mesmo ID.
        return alunos.putIfAbsent(aluno.id(), aluno) == null;
    }

    public Optional<Aluno> buscar(int id) {
        return Optional.ofNullable(alunos.get(id));
    }
}

package alunos;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

public class Comparacao {
    public static void main(String[] args) throws Exception {
        Aluno aluno = new Aluno();
        aluno.id = 1024;
        aluno.nome = "Ana Silva";
        aluno.curso = "Engenharia de Software";

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(aluno);
        Aluno recuperado = mapper.readValue(json, Aluno.class);

        System.out.println("JSON: " + json);
        System.out.println("Aluno recuperado: " + recuperado.id + ", "
                + recuperado.nome + ", " + recuperado.curso);

        AlunoProtos.Aluno alunoProto = AlunoProtos.Aluno.newBuilder()
                .setId(aluno.id)
                .setNome(aluno.nome)
                .setCurso(aluno.curso)
                .build();

        System.out.println("JSON: " + json.getBytes(StandardCharsets.UTF_8).length + " bytes");
        System.out.println("Protobuf: " + alunoProto.toByteArray().length + " bytes");
    }
}

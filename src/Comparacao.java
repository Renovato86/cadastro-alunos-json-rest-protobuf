import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

public class Comparacao {
    public static void main(String[] args) throws Exception {
        Aluno aluno = new Aluno(1024, "Ana Silva", "Engenharia de Software");
        ObjectMapper mapper = new ObjectMapper();

        // Converte o objeto em JSON e depois recupera o objeto.
        String json = mapper.writeValueAsString(aluno);
        Aluno recuperado = mapper.readValue(json, Aluno.class);

        System.out.println("JSON: " + json);
        System.out.println("Aluno recuperado: " + recuperado.getId() + ", "
                + recuperado.getNome() + ", " + recuperado.getCurso());

        // Usa a classe gerada a partir de aluno.proto.
        AlunoProtos.Aluno alunoProto = AlunoProtos.Aluno.newBuilder()
                .setId(aluno.getId())
                .setNome(aluno.getNome())
                .setCurso(aluno.getCurso())
                .build();

        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        byte[] protobufBytes = alunoProto.toByteArray();

        System.out.println("JSON: " + jsonBytes.length + " bytes");
        System.out.println("Protobuf: " + protobufBytes.length + " bytes");
    }
}

package br.edu.atividade.alunos;

import br.edu.atividade.alunos.proto.AlunoProtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.Locale;

public class SerializacaoDemo {
    public static AlunoProtos.Aluno paraProtobuf(Aluno aluno) {
        return AlunoProtos.Aluno.newBuilder()
                .setId(aluno.id()).setNome(aluno.nome()).setCurso(aluno.curso()).build();
    }

    public static Aluno deProtobuf(AlunoProtos.Aluno aluno) {
        return new Aluno(aluno.getId(), aluno.getNome(), aluno.getCurso());
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Aluno original = new Aluno(1024, "Ana Silva", "Engenharia de Software");

        // JSON: objeto -> texto -> bytes UTF-8 -> objeto.
        String json = mapper.writeValueAsString(original);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        Aluno deJson = mapper.readValue(jsonBytes, Aluno.class);

        // Protobuf: classe gerada do .proto -> bytes binários -> objeto.
        byte[] protobufBytes = paraProtobuf(original).toByteArray();
        Aluno deBinario = deProtobuf(AlunoProtos.Aluno.parseFrom(protobufBytes));
        if (!original.equals(deJson) || !original.equals(deBinario)) {
            throw new IllegalStateException("Os dados reconstruídos diferem do original");
        }

        double reducao = 100.0 * (jsonBytes.length - protobufBytes.length) / jsonBytes.length;
        String relatorio = String.format(Locale.ROOT, """
                Aluno utilizado: %s
                JSON compacto (UTF-8): %s
                JSON: %d bytes
                Protobuf: %d bytes
                Redução neste exemplo: %.2f%%
                Protobuf em hexadecimal: %s
                Desserialização JSON: %s
                Desserialização Protobuf: %s
                Igualdade com o original: confirmada nos dois formatos

                Medição apenas do corpo serializado, sem cabeçalhos HTTP,
                compressão, contrato .proto ou classes geradas.
                O hexadecimal serve para inspeção; seu tamanho textual não é
                o tamanho do arquivo binário. Isto não é um benchmark de velocidade.
                """, original, json, jsonBytes.length, protobufBytes.length, reducao,
                HexFormat.ofDelimiter(" ").formatHex(protobufBytes), deJson, deBinario);
        Path destino = Path.of(args.length > 0 ? args[0] : "resultados");
        Files.createDirectories(destino);
        Files.write(destino.resolve("aluno.json"), jsonBytes);
        Files.write(destino.resolve("aluno.pb"), protobufBytes);
        Files.writeString(destino.resolve("comparacao.txt"), relatorio, StandardCharsets.UTF_8);
        System.out.print(relatorio);
    }
}

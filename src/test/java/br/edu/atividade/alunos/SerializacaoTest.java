package br.edu.atividade.alunos;

import br.edu.atividade.alunos.proto.AlunoProtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SerializacaoTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void doisFormatosReconstruemOMesmoAlunoComAcentos() throws Exception {
        Aluno original = new Aluno(1024, "João São José", "Ciência da Computação");
        byte[] json = mapper.writeValueAsBytes(original);
        byte[] proto = SerializacaoDemo.paraProtobuf(original).toByteArray();
        assertThat(mapper.readValue(json, Aluno.class)).isEqualTo(original);
        assertThat(SerializacaoDemo.deProtobuf(AlunoProtos.Aluno.parseFrom(proto))).isEqualTo(original);
        // UTF-8 pode usar mais de um byte por caractere.
        assertThat(json.length).isGreaterThan(new String(json, StandardCharsets.UTF_8).length());
    }

    @Test
    void contratoInterpretaMensagemBinariaConhecida() throws Exception {
        // Campo 1: id=150; campo 2: nome="Ana"; campo 3: curso="SI".
        byte[] bytes = HexFormat.of().parseHex("0896011203416e611a025349");
        Aluno aluno = SerializacaoDemo.deProtobuf(AlunoProtos.Aluno.parseFrom(bytes));
        assertThat(aluno).isEqualTo(new Aluno(150, "Ana", "SI"));
        assertThat(SerializacaoDemo.paraProtobuf(aluno).toByteArray()).isEqualTo(bytes);
    }

    @Test
    void mensagemPreservaCamposDesconhecidosParaEvolucaoDoContrato() throws Exception {
        // Campo 4 desconhecido para este esquema, com valor 7.
        byte[] bytes = HexFormat.of().parseHex("0896011203416e611a0253492007");
        AlunoProtos.Aluno recebido = AlunoProtos.Aluno.parseFrom(bytes);
        assertThat(recebido.getId()).isEqualTo(150);
        assertThat(recebido.getUnknownFields().hasField(4)).isTrue();
        assertThat(AlunoProtos.Aluno.parseFrom(recebido.toByteArray()).getUnknownFields().hasField(4))
                .isTrue();
    }
}

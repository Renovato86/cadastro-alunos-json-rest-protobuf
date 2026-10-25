package br.edu.atividade.alunos;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AlunoApiTest {
    @Autowired TestRestTemplate http;

    private ResponseEntity<String> post(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return http.postForEntity("/alunos", new HttpEntity<>(json, headers), String.class);
    }

    @Test
    void cadastroEConsultaPreservamDadosECabecalhos() {
        Aluno aluno = new Aluno(101, "João São José", "Ciência da Computação");
        ResponseEntity<Aluno> criado = http.postForEntity("/alunos", aluno, Aluno.class);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(criado.getHeaders().getLocation()).hasToString("/alunos/101");
        assertThat(criado.getHeaders().getContentType()).isNotNull()
                .satisfies(tipo -> assertThat(tipo.isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue());
        assertThat(criado.getBody()).isEqualTo(aluno);
        ResponseEntity<Aluno> consultado = http.getForEntity("/alunos/101", Aluno.class);
        assertThat(consultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consultado.getBody()).isEqualTo(aluno);
    }

    @Test
    void consultaInexistenteRetorna404() {
        ResponseEntity<ApiExceptionHandler.Erro> resposta = http.getForEntity(
                "/alunos/999999", ApiExceptionHandler.Erro.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).isEqualTo(new ApiExceptionHandler.Erro(404, "Aluno não encontrado"));
    }

    @Test
    void duplicadoNaoSobrescreveCadastroOriginal() {
        Aluno original = new Aluno(102, "Ana", "Computação");
        assertThat(http.postForEntity("/alunos", original, String.class).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        ResponseEntity<String> repetido = http.postForEntity("/alunos",
                new Aluno(102, "Outro nome", "Outro curso"), String.class);
        assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(http.getForObject("/alunos/102", Aluno.class)).isEqualTo(original);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"id\":0,\"nome\":\"Ana\",\"curso\":\"Computação\"}",
        "{\"id\":-1,\"nome\":\"Ana\",\"curso\":\"Computação\"}",
        "{\"nome\":\"Ana\",\"curso\":\"Computação\"}",
        "{\"id\":200,\"nome\":\"  \",\"curso\":\"Computação\"}",
        "{\"id\":201,\"nome\":\"Ana\"}",
        "{\"id\":202,\"nome\":\"Ana\",\"curso\":null}",
        "{\"id\":\"abc\",\"nome\":\"Ana\",\"curso\":\"Computação\"}",
        "{\"id\":203,\"nome\":\"Ana\",\"curso\":\"Computação\",\"extra\":1}",
        "{json invalido"
    })
    void rejeitaCorpoInvalido(String json) {
        assertThat(post(json).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "2147483648"})
    void rejeitaIdInvalidoNaConsulta(String id) {
        assertThat(http.getForEntity("/alunos/" + id, String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejeitaTipoDeConteudoIncorreto() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        assertThat(http.postForEntity("/alunos", new HttpEntity<>("texto", headers), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
}

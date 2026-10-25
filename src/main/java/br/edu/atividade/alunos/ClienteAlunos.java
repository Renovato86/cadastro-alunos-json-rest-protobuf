package br.edu.atividade.alunos;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ClienteAlunos {
    public static void main(String[] args) throws IOException, InterruptedException {
        String base = (args.length > 0 ? args[0] : "http://localhost:8080").replaceAll("/+$", "");
        int id = args.length > 1 ? Integer.parseInt(args[1]) : 1024;
        Aluno aluno = new Aluno(id, "Ana Silva", "Engenharia de Software");
        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        HttpRequest post = HttpRequest.newBuilder(URI.create(base + "/alunos"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(aluno), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> criado = client.send(post, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.out.println("POST /alunos -> " + criado.statusCode() + " " + criado.body());
        if (criado.statusCode() != 201 && criado.statusCode() != 409) {
            throw new IOException("Falha no cadastro: HTTP " + criado.statusCode());
        }
        if (criado.statusCode() == 409) {
            System.out.println("ID já cadastrado; consultando o registro existente.");
        }

        HttpRequest get = HttpRequest.newBuilder(URI.create(base + "/alunos/" + id))
                .timeout(Duration.ofSeconds(5)).header("Accept", "application/json").GET().build();
        HttpResponse<String> recebido = client.send(get, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.out.println("GET /alunos/" + id + " -> " + recebido.statusCode() + " " + recebido.body());
        if (recebido.statusCode() != 200) {
            throw new IOException("Falha na consulta: HTTP " + recebido.statusCode());
        }
        Aluno reconstruido = mapper.readValue(recebido.body(), Aluno.class);
        System.out.println("Objeto reconstruído com Jackson: " + reconstruido);
    }
}

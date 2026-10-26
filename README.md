# Cadastro de alunos

A pasta `alunos` contém o código: `Aluno.java` guarda os dados, `Servidor.java` cadastra e consulta, e `Comparacao.java` compara JSON e Protobuf. O contrato é `aluno.proto`.

## Executar

É necessário Java 17 a 25 e Maven. Os comandos abaixo são executados na pasta do projeto. A primeira compilação precisa de internet para baixar as bibliotecas.

```text
mvn clean package
mvn spring-boot:run
```

O servidor abre em `http://localhost:8080`. A página inicial tem formulário de cadastro, lista dos alunos e busca por ID, nome ou curso. A lista é atualizada automaticamente a cada cinco segundos enquanto a página está visível. IDs repetidos pedem confirmação na página antes de substituir o cadastro. A API permite IDs inteiros positivos (até 2147483647) e exige nome e curso preenchidos. Os dados ficam na memória e somem ao encerrar. Para parar, use Ctrl+C.

Em outro terminal, teste com curl:

```text
curl.exe -i -X POST http://localhost:8080/alunos -H "Content-Type: application/json" --data-binary "@aluno.json"
curl.exe -i http://localhost:8080/alunos/1024
curl.exe -i http://localhost:8080/alunos/999999
```

Os resultados são **201** para cadastro novo, **200** para consulta e **404** para aluno inexistente. Usar novamente o mesmo ID substitui os dados e retorna 200. Em Linux/macOS, use `curl` no lugar de `curl.exe`.

## Comparação

Não precisa deixar o servidor aberto para executar:

```text
mvn compile exec:java "-Dexec.mainClass=alunos.Comparacao"
```

Jackson transforma o aluno em JSON e recupera o objeto. O Maven gera a classe Java do contrato Protobuf durante a compilação, dentro de `target/gerado`. A pasta `target` é criada automaticamente e contém os arquivos compilados.

JSON gerado:

```json
{"id":1024,"nome":"Ana Silva","curso":"Engenharia de Software"}
```

| Critério | JSON | Protobuf |
| --- | --- | --- |
| Legibilidade | Texto legível | Binário, exige ferramentas |
| Tamanho deste aluno | 63 bytes | 38 bytes |
| Esquema | Não exige arquivo de esquema separado | Usa o arquivo `.proto` |

A contagem usa JSON compacto em UTF-8 e Protobuf binário, sem cabeçalhos HTTP. O tamanho varia conforme os dados.

JSON é útil em APIs REST pela facilidade de leitura e integração. Protobuf pode ser útil quando os sistemas compartilham o contrato e precisam transmitir menos bytes. Nesta atividade a API usa JSON, e Protobuf é usado na comparação.

## Usar em dois computadores

No computador principal, execute na pasta do projeto e deixe o terminal aberto:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.address=0.0.0.0"
```

No principal, abra `http://localhost:8080/`. No segundo computador, conectado à mesma rede ou Radmin VPN, abra `http://IP_DO_PRINCIPAL:8080/`, substituindo o IP. Não é necessário instalar Java ou Maven no segundo computador. Libere a porta TCP 8080 no firewall do principal para a conexão utilizada se o acesso for bloqueado.

Os dois computadores devem acessar o mesmo servidor para compartilhar a lista. Reiniciar o servidor apaga os cadastros, pois o armazenamento continua em memória. A tela e a API usam JSON; a comparação com Protobuf permanece separada.

### Rotas

- `GET /`: formulário e lista.
- `GET /alunos`: lista em JSON, ordenada pelo ID.
- `GET /alunos/{id}`: consulta um aluno; retorna 404 se não existir.
- `POST /alunos`: cadastra (201) ou substitui um ID existente (200). Retorna 400 para ID inválido ou nome/curso em branco.

A confirmação de substituição é uma conveniência da página. A API mantém o comportamento de substituir um ID repetido; se dois computadores gravarem o mesmo ID simultaneamente, vale a última gravação.

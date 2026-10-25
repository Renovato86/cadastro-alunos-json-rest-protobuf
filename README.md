# Cadastro de Alunos com JSON, REST e Protobuf

Implementação em Java da atividade prática da página 29 do material **Comunicação Interprocesso**, do professor Leandro Alexandre Freitas (IFG).

O projeto cadastra e consulta alunos por uma API REST, usa Jackson para serialização/desserialização JSON e gera classes Java a partir de um contrato Protobuf. Inclui cliente Java, 20 testes automatizados e comparação reproduzível dos formatos.

## Requisitos

- JDK 17 a 25; compilação configurada para Java 17. Validado com JDK 25.0.1.
- Internet na primeira execução para baixar Maven, bibliotecas e o compilador Protobuf.
- Não é necessário instalar Maven nem `protoc`: o Maven Wrapper e o projeto cuidam desses downloads.
- Spring Boot 3.5.16, Protobuf 4.36.2 e Maven 3.9.11.

Confira `java -version` e `javac -version`. Se houver mais de um JDK, configure `JAVA_HOME` para um dos JDKs compatíveis; versões antigas do Java não executam este projeto.

## Executar no Windows

Extraia o ZIP e abra o terminal dentro da pasta `cadastro-alunos`.

```powershell
# Compila, gera as classes Protobuf e executa os testes.
.\mvnw.cmd clean verify

# Inicia a API (deixe este terminal aberto).
.\mvnw.cmd spring-boot:run
```

A API inicia em `http://localhost:8080`. Para parar, pressione `Ctrl+C`.

Também é possível executar o arquivo compilado:

```powershell
java -jar target/cadastro-alunos-1.0.0.jar
```

Linux/macOS: use `sh ./mvnw` no lugar de `.\mvnw.cmd` nos comandos Maven. Há suporte automático à identificação do sistema/arquitetura pelo plugin Protobuf, sujeito à disponibilidade de um binário oficial de `protoc` para a plataforma.

## Testar com curl

Em outro terminal, na mesma pasta, execute estes comandos. No PowerShell, use `curl.exe` para evitar o alias antigo de `Invoke-WebRequest`.

```powershell
# 201 Created; inclui Location: /alunos/1024.
curl.exe -i -X POST http://localhost:8080/alunos -H "Content-Type: application/json" --data-binary "@exemplos/aluno.json"

# 200 OK; devolve o aluno cadastrado.
curl.exe -i http://localhost:8080/alunos/1024

# 404 Not Found; aluno não cadastrado em um servidor recém-iniciado.
curl.exe -i http://localhost:8080/alunos/999999

# Repetir o POST anterior produz 409 Conflict e preserva o aluno original.
```

No Linux/macOS, substitua `curl.exe` por `curl`. O arquivo de exemplo contém:

```json
{"id":1024,"nome":"Ana Silva","curso":"Engenharia de Software"}
```

| Método e caminho | Resultado |
| --- | --- |
| `POST /alunos` | `201` + aluno criado + cabeçalho `Location` |
| `GET /alunos/{id}` | `200` + aluno encontrado |
| `GET /alunos/{id}` inexistente | `404` + erro JSON |
| `POST /alunos` com ID já utilizado | `409` + erro JSON |
| Dados obrigatórios ausentes, vazios ou inválidos | `400` |
| `POST` com tipo de conteúdo diferente de JSON | `415` |

O ID é informado pelo cliente e deve ser um inteiro positivo de 32 bits (até 2.147.483.647). Nome e curso são obrigatórios e aceitam até 120 caracteres. Campos JSON desconhecidos são rejeitados. Exemplo de erro da consulta:

```json
{"status":404,"mensagem":"Aluno não encontrado"}
```

## Consumir a API com o cliente Java

Com o servidor aberto, execute:

```powershell
.\mvnw.cmd compile exec:java "-Dexec.mainClass=br.edu.atividade.alunos.ClienteAlunos"
```

O cliente usa `java.net.http.HttpClient`, envia o aluno com `POST`, consulta com `GET` e reconstrói o objeto com Jackson. Se o ID já existir, informa o conflito e consulta o registro existente. Possui timeout de conexão de 3 segundos e de requisição de 5 segundos; uma falha de rede gera exceção e código de saída de erro.

Para alterar endereço e ID:

```powershell
.\mvnw.cmd compile exec:java "-Dexec.mainClass=br.edu.atividade.alunos.ClienteAlunos" "-Dexec.args=http://localhost:8080 2048"
```

## Comparar JSON e Protobuf

Esta demonstração não precisa do servidor:

```powershell
.\mvnw.cmd compile exec:java "-Dexec.mainClass=br.edu.atividade.alunos.SerializacaoDemo"
```

O comando grava `resultados/aluno.json`, `resultados/aluno.pb` e `resultados/comparacao.txt`. Mede os bytes reais e verifica a igualdade dos objetos reconstruídos nos dois formatos.

| Formato | Tamanho medido para o mesmo aluno |
| --- | ---: |
| JSON compacto em UTF-8 | 63 bytes |
| Protobuf binário | 38 bytes |
| Redução do Protobuf neste exemplo | 39,68% |

Os resultados não incluem cabeçalhos HTTP, compressão ou arquivos de esquema. O arquivo JSON de `resultados/` não tem quebra de linha final; o arquivo de requisição em `exemplos/` tem uma quebra de linha e pode ter um byte adicional. O relatório completo está em [RELATORIO.md](RELATORIO.md).

## Organização

```text
src/main/java/br/edu/atividade/alunos/
  Aluno.java                       Classe de dados e validações
  AlunoController.java             Endpoints REST
  AlunoRepository.java             Cadastro em memória
  ApiExceptionHandler.java         Respostas de erro
  CadastroAlunosApplication.java   Inicialização Spring Boot
  ClienteAlunos.java               Cliente HTTP em Java
  SerializacaoDemo.java            JSON, Protobuf e medição
src/main/proto/aluno.proto         Contrato Protobuf
src/test/java/                    Testes de API e serialização
exemplos/aluno.json                Corpo para o curl
resultados/                       Medições e evidências executadas
```

O Maven gera `target/generated-sources/protobuf/java/br/edu/atividade/alunos/proto/AlunoProtos.java` na fase `generate-sources`. Não edite a classe gerada: altere `src/main/proto/aluno.proto` e compile novamente. O `protoc` e a biblioteca Java usam a mesma versão.

## Testes e limites

`clean verify` executa 20 casos: criação/consulta por HTTP real, cabeçalho `Location`, preservação de acentos, inexistência, duplicidade sem sobrescrita, nove corpos inválidos, quatro IDs inválidos, tipo de conteúdo incorreto, reconstrução JSON/Protobuf, mensagem binária conhecida e preservação de campos Protobuf desconhecidos pela classe gerada.

Os dados ficam em memória e são perdidos ao reiniciar. A API é didática, sem banco de dados ou autenticação, e escuta apenas no computador local. Nesta atividade a API troca JSON; o Protobuf é usado na comparação independente. Não há implementação de gRPC nem negociação de conteúdo binário nos endpoints.

## Referências

- Material da disciplina: **Comunicação Interprocesso**, página 29, arquivo fornecido pelo usuário.
- [Requisitos do Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
- [Tutorial oficial de Protobuf para Java](https://protobuf.dev/getting-started/javatutorial/).
- [Código Java gerado pelo Protobuf](https://protobuf.dev/reference/java/java-generated/).

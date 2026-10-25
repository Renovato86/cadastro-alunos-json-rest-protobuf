# Relatório da atividade prática

## Implementação

O aluno é representado pela classe Java `Aluno`, declarada como `record`, com `id`, `nome` e `curso`. O Spring Boot disponibiliza `POST /alunos` para cadastro e `GET /alunos/{id}` para consulta. O cadastro retorna `201 Created`, a consulta bem-sucedida retorna `200 OK` e a consulta de um ID inexistente retorna `404 Not Found`.

O Spring MVC usa Jackson para converter o corpo JSON recebido em um objeto Java e para converter o retorno do controlador em JSON. A classe `SerializacaoDemo` também mostra essas etapas explicitamente com `writeValueAsString` e `readValue`. O cliente Java envia requisições HTTP e desserializa a resposta recebida.

O armazenamento utiliza um mapa em memória. A inserção atômica com `putIfAbsent` impede que um cadastro duplicado sobrescreva outro aluno; a API responde `409 Conflict`. As validações de entrada geram `400 Bad Request`.

## Objeto e contrato utilizados

Objeto do experimento: ID 1024, nome Ana Silva e curso Engenharia de Software.

JSON registrado pelo programa, compacto e codificado em UTF-8:

```json
{"id":1024,"nome":"Ana Silva","curso":"Engenharia de Software"}
```

Contrato lógico em `src/main/proto/aluno.proto`:

```proto
syntax = "proto3";
message Aluno {
  int32 id = 1;
  string nome = 2;
  string curso = 3;
}
```

O arquivo real também especifica os pacotes e o nome da classe Java gerada. O Maven executa `protoc` para produzir `AlunoProtos.Aluno`. O programa usa `toByteArray()` para serializar e `parseFrom()` para desserializar.

## Comparação medida

| Critério | JSON | Protobuf |
| --- | --- | --- |
| Representação | Texto UTF-8 | Binário |
| Legibilidade | Campos e valores podem ser lidos diretamente | Exige ferramentas e conhecimento do contrato para interpretação semântica |
| Tamanho deste objeto | **63 bytes** | **38 bytes** |
| Dependência de esquema | A sintaxe dispensa um arquivo externo; o significado ainda exige um contrato acordado. JSON Schema pode formalizá-lo. | O `.proto` define nomes, tipos e números dos campos; gera código para os participantes. |
| Integração | Conveniente para navegadores, curl e clientes variados | Exige biblioteca e código/descritor compatível com o esquema |
| Evolução | Depende das regras de aceitação e validação do consumidor | Números e tipos de campos precisam evoluir de modo compatível |

Redução observada: `(63 - 38) / 63 × 100 = 39,68%`. Este resultado é específico deste objeto e destas configurações. Não demonstra que Protobuf sempre tenha o mesmo ganho nem mede tempo de processamento.

Bytes Protobuf registrados em hexadecimal:

```text
08 80 08 12 09 41 6e 61 20 53 69 6c 76 61 1a 16 45 6e 67
65 6e 68 61 72 69 61 20 64 65 20 53 6f 66 74 77 61 72 65
```

O cálculo usa `json.getBytes(StandardCharsets.UTF_8).length` e `mensagem.toByteArray().length`. Contar caracteres com `String.length()` pode produzir resultado incorreto quando há acentos. O teste com João São José e Ciência da Computação verifica esse caso.

As duas desserializações reconstruíram o aluno original. A comparação considera apenas o corpo da mensagem, sem HTTP, TLS, compressão, o arquivo `.proto` ou as classes geradas. O texto hexadecimal acima é uma visualização dos bytes, não o formato binário enviado.

## Quando usar cada formato em uma API REST?

**JSON é preferível** quando a API atende navegadores, integrações públicas ou equipes com ferramentas variadas e quando a inspeção manual das mensagens é importante. O texto facilita depuração com curl, Postman e ferramentas do navegador. Neste cadastro simples, essa facilidade costuma pesar mais que a economia de algumas dezenas de bytes.

**Protobuf pode ser preferível** quando os participantes compartilham um contrato bem definido, há muitas mensagens e reduzir o volume transmitido é relevante. É uma alternativa para comunicações entre serviços ou clientes controlados pela mesma equipe. A escolha deve ser confirmada por medições com dados reais, pois compressão, tamanho dos campos e custos de integração mudam o resultado.

REST não obriga o uso de JSON. Uma API HTTP pode oferecer uma representação Protobuf, desde que cliente e servidor acordem o esquema, implementem a serialização binária e usem cabeçalhos `Content-Type` e `Accept` coerentes. Esses endpoints binários não foram implementados aqui porque o enunciado solicita troca JSON na API e comparação Protobuf separada. Protobuf é um formato de serialização; usá-lo não transforma uma API REST em gRPC.

## Marshalling, falhas e evolução

A serialização transforma o objeto em uma representação externa. Na requisição completa, o cliente também escolhe método, URL e cabeçalhos. Essa preparação mais ampla participa do marshalling. No servidor, o corpo é desserializado, validado e processado; a resposta percorre o caminho inverso.

Um timeout não prova que o cadastro falhou: o servidor pode ter salvo o aluno antes de a resposta se perder. Nesta implementação, repetir o POST com o mesmo ID retorna `409`, e uma consulta permite verificar o cadastro existente. O cliente possui limites de espera para evitar bloqueio indefinido.

Na evolução Protobuf, os números de campos não devem ser reaproveitados para outro significado. Campos removidos devem ter seus números e nomes reservados. A classe gerada preserva campos desconhecidos ao ler e reserializar a mensagem; convertê-la para o `record Aluno` elimina dados que esse record não representa. No JSON deste projeto, campos desconhecidos são rejeitados deliberadamente; essa política deve ser revista se for necessário aceitar clientes de outras versões.

## Evidências e reprodução

Execução local em 25/10/2026, com JDK 25.0.1, Maven 3.9.11, Spring Boot 3.5.16 e Protobuf 4.36.2. Os 20 testes passaram sem falhas ou erros. Os testes de integração iniciam um servidor em porta aleatória e fazem requisições HTTP reais.

- `resultados/comparacao.txt`: saída produzida pelo programa Java.
- `resultados/aluno.json` e `resultados/aluno.pb`: os corpos medidos.
- `resultados/testes.txt`: resumo da execução dos testes.
- `resultados/curl.txt`: respostas observadas com curl em um servidor local de teste.
- `resultados/cliente-java.txt`: execução do consumidor Java.

Os comandos de reprodução estão no README. Os alunos usados são dados fictícios do exemplo didático.

## Referências

- **Comunicação Interprocesso**, professor Leandro Alexandre Freitas, IFG, página 29 (material fornecido).
- [Tutorial oficial de Protobuf para Java](https://protobuf.dev/getting-started/javatutorial/).
- [Guia de classes Java geradas](https://protobuf.dev/reference/java/java-generated/).
- [Requisitos oficiais do Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

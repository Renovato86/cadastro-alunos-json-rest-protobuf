# Cadastro de alunos com JSON, REST e Protobuf

Projeto em Java com Spring Boot. O aluno tem os campos `id`, `nome` e `curso`.

O código fica em uma única pasta, `src`:

```text
src/
  Aluno.java
  AlunoController.java
  Aplicacao.java
  Comparacao.java
  aluno.proto
  application.properties
```

`Aluno` guarda os dados, `AlunoController` recebe as requisições, `Aplicacao` inicia o servidor e `Comparacao` demonstra JSON e Protobuf.

## Executar

É necessário JDK 17 a 25. O projeto foi executado com JDK 25. Na primeira execução é preciso ter internet para baixar as dependências. Os arquivos `mvnw`, `mvnw.cmd` e a pasta `.mvn` permitem executar o Maven sem instalação separada. A pasta `target` aparece automaticamente ao compilar e guarda os arquivos gerados.

No terminal, dentro da pasta do projeto:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

O servidor fica em `http://localhost:8080`. Os alunos ficam guardados em memória e são perdidos quando o programa é encerrado. Use `Ctrl+C` para parar.

## Testar a API

Com o servidor aberto, execute em outro terminal na pasta do projeto:

```powershell
curl.exe -i -X POST http://localhost:8080/alunos -H "Content-Type: application/json" --data-binary "@aluno.json"
curl.exe -i http://localhost:8080/alunos/1024
curl.exe -i http://localhost:8080/alunos/999999
```

O primeiro comando cadastra o aluno e retorna **201**. O segundo consulta o aluno e retorna **200** com seus dados em JSON. O terceiro retorna **404**, pois esse ID não está cadastrado. Repetir um cadastro com o mesmo ID retorna **409**, para evitar duplicidade.

No Linux/macOS, use `sh ./mvnw` no lugar de `.\mvnw.cmd` e `curl` no lugar de `curl.exe`.

## JSON e Protobuf

Para executar a conversão e a comparação, sem precisar iniciar o servidor:

```powershell
.\mvnw.cmd compile exec:java "-Dexec.mainClass=Comparacao"
```

O programa cria um aluno, converte para JSON com Jackson e recupera o objeto a partir desse JSON. Depois serializa o mesmo aluno em Protobuf e conta os bytes dos dois formatos.

JSON gerado:

```json
{"id":1024,"nome":"Ana Silva","curso":"Engenharia de Software"}
```

Objeto recuperado: ID 1024, nome Ana Silva, curso Engenharia de Software.

O contrato está em `src/aluno.proto`. Na compilação, o Maven executa o compilador Protobuf e gera a classe `AlunoProtos` em `target/gerado/AlunoProtos.java`.

| Comparação | JSON | Protobuf |
| --- | --- | --- |
| Legibilidade | Texto que pode ser lido diretamente | Binário, precisa de ferramentas para interpretar |
| Tamanho do aluno usado no exemplo | 63 bytes | 38 bytes |
| Esquema | Não exige um arquivo de esquema separado | Usa o contrato definido no arquivo `.proto` |

A medição considera o JSON compacto em UTF-8 e os bytes binários do Protobuf, sem os cabeçalhos HTTP. Os tamanhos podem mudar conforme os dados.

JSON é útil em APIs REST quando a facilidade de leitura e integração é mais importante, por exemplo em aplicações usadas por navegadores. Protobuf pode ser útil quando os sistemas compartilham um contrato e precisam reduzir o volume de dados transmitidos.

Nesta atividade, a API troca JSON. Protobuf é usado na comparação de serialização. Uma API REST também pode usar Protobuf, desde que cliente e servidor estejam preparados para esse formato.

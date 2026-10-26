# Cadastro de alunos

A pasta `alunos` contém o código: `Aluno.java` guarda os dados, `Servidor.java` cadastra e consulta, e `Comparacao.java` compara JSON e Protobuf. O contrato é `aluno.proto`.

## Executar

É necessário Java 17 a 25 e Maven. Os comandos abaixo são executados na pasta do projeto. A primeira compilação precisa de internet para baixar as bibliotecas.

```text
mvn clean package
mvn spring-boot:run
```

O servidor atende em `http://localhost:8080`. Use o cliente de terminal abaixo para cadastrar, listar e consultar alunos. Os dados ficam na memória e somem ao encerrar. Para parar, use Ctrl+C.

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

## Menu no terminal

Com o servidor Java aberto, inicie outro PowerShell na pasta do projeto:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\cliente.ps1 -Servidor http://localhost:8080
```

O menu oferece:

1. Cadastrar aluno (pede ID, nome e curso).
2. Listar todos os alunos.
3. Consultar um aluno pelo ID.
0. Sair do cliente sem encerrar o servidor.

Use IDs inteiros positivos (1 a 2147483647), nome e curso preenchidos. Um ID repetido pede confirmação antes de substituir os dados. O cliente envia JSON em UTF-8 e funciona no Windows PowerShell 5.1 ou PowerShell 7. A opção de execução vale apenas para o processo iniciado pelo comando.

## Usar em dois computadores

No principal, execute e deixe o terminal aberto:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.address=0.0.0.0"
```

No segundo computador, conectado à mesma rede ou Radmin VPN, execute o cliente:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\cliente.ps1 -Servidor http://IP_DO_PRINCIPAL:8080
```

Substitua IP_DO_PRINCIPAL pelo IPv4 do principal no Radmin VPN. Basta copiar `cliente.ps1` para o segundo computador; ele não precisa de Java nem Maven. No principal, o cliente deve ficar em outro terminal, separado do servidor. Se necessário, libere TCP 8080 no firewall para a conexão utilizada.

Os dois clientes acessam a mesma API. Escolha listar ou consultar para obter os dados atuais; não há atualização automática do terminal. Os cadastros continuam em memória e somem quando o servidor é encerrado. Sair apenas do cliente não apaga os dados.

### Rotas da API JSON

- `GET /alunos`: lista em JSON ordenada por ID.
- `GET /alunos/{id}`: consulta; retorna 404 se não existir.
- `POST /alunos`: cadastra (201) ou substitui ID existente (200). ID inválido ou nome/curso em branco retorna 400.

A API substitui IDs repetidos. A confirmação pertence ao cliente; em gravações simultâneas para o mesmo ID, vale a última gravação. A comparação JSON/Protobuf continua separada da API. Este projeto não inclui interface web.

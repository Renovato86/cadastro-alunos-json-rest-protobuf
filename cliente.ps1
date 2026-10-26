# Menu para Windows PowerShell 5.1 ou PowerShell 7.
# O cliente usa a API JSON do servidor Java, inclusive pela rede Radmin VPN.
[CmdletBinding()]
param(
    [string]$Servidor = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'
$Servidor = $Servidor.TrimEnd('/')
[uri]$endereco = $null
if (-not [uri]::TryCreate($Servidor, [UriKind]::Absolute, [ref]$endereco) -or
    $endereco.Scheme -notin @('http', 'https')) {
    throw 'Informe o servidor no formato http://IP:8080.'
}

function Ler-Id {
    $texto = Read-Host 'ID do aluno (numero inteiro positivo)'
    [int]$numero = 0
    if (-not [int]::TryParse($texto, [ref]$numero) -or $numero -le 0) {
        Write-Host 'ID invalido. Use um numero de 1 a 2147483647.' -ForegroundColor Yellow
        return $null
    }
    return $numero
}

function Consultar-Aluno([int]$Numero) {
    try {
        return Invoke-RestMethod -Uri "$Servidor/alunos/$Numero" -Method Get -TimeoutSec 15
    }
    catch {
        if ($null -ne $_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 404) {
            return $null
        }
        throw
    }
}

function Cadastrar-Aluno {
    $numero = Ler-Id
    if ($null -eq $numero) { return }
    $nome = (Read-Host 'Nome completo').Trim()
    $curso = (Read-Host 'Curso').Trim()
    if ([string]::IsNullOrWhiteSpace($nome) -or [string]::IsNullOrWhiteSpace($curso)) {
        Write-Host 'Nome e curso precisam ser preenchidos.' -ForegroundColor Yellow
        return
    }
    $anterior = Consultar-Aluno $numero
    if ($null -ne $anterior) {
        Write-Host "O ID $numero ja esta cadastrado para $($anterior.nome)." -ForegroundColor Yellow
        if ((Read-Host 'Substituir esse cadastro? Digite S para confirmar') -ine 'S') {
            Write-Host 'Cadastro cancelado. Nenhum dado foi alterado.'
            return
        }
    }
    $json = @{ id = $numero; nome = $nome; curso = $curso } | ConvertTo-Json -Compress
    # Bytes UTF-8 preservam acentos tambem no Windows PowerShell 5.1.
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    try {
        $salvo = Invoke-RestMethod -Uri "$Servidor/alunos" -Method Post -ContentType 'application/json; charset=utf-8' -Body $bytes -TimeoutSec 15
    }
    catch {
        Write-Host 'Nao foi possivel confirmar o salvamento. Consulte o ID antes de tentar novamente.' -ForegroundColor Yellow
        throw
    }
    Write-Host 'Cadastro salvo com sucesso.' -ForegroundColor Green
    $salvo | Select-Object id, nome, curso | Format-List | Out-Host
}

function Listar-Alunos {
    $resposta = Invoke-RestMethod -Uri "$Servidor/alunos" -Method Get -TimeoutSec 15
    $lista = @($resposta | Where-Object { $null -ne $_ })
    if ($lista.Count -eq 0) {
        Write-Host 'Nenhum aluno cadastrado.'
        return
    }
    $lista | Sort-Object id | Select-Object id, nome, curso | Format-Table -AutoSize -Wrap | Out-Host
    Write-Host "Total: $($lista.Count) aluno(s)."
}

function Mostrar-Aluno {
    $numero = Ler-Id
    if ($null -eq $numero) { return }
    $aluno = Consultar-Aluno $numero
    if ($null -eq $aluno) {
        Write-Host "Aluno com ID $numero nao encontrado." -ForegroundColor Yellow
        return
    }
    $aluno | Select-Object id, nome, curso | Format-List | Out-Host
}

Write-Host "Servidor: $Servidor" -ForegroundColor Cyan
Write-Host 'Os dados sao compartilhados pelo servidor e somem quando ele e encerrado.'
:menu while ($true) {
    Write-Host ''
    Write-Host '=== CADASTRO DE ALUNOS ===' -ForegroundColor Cyan
    Write-Host '1 - Cadastrar aluno'
    Write-Host '2 - Listar todos os alunos'
    Write-Host '3 - Consultar aluno pelo ID'
    Write-Host '0 - Sair do menu'
    $opcao = Read-Host 'Escolha uma opcao'
    try {
        switch ($opcao) {
            '1' { Cadastrar-Aluno }
            '2' { Listar-Alunos }
            '3' { Mostrar-Aluno }
            '0' { break menu }
            default { Write-Host 'Opcao invalida. Digite 1, 2, 3 ou 0.' -ForegroundColor Yellow }
        }
    }
    catch {
        Write-Host "Falha ao acessar $Servidor. Confira o servidor, a conexao e o Radmin VPN." -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor DarkYellow
    }
}

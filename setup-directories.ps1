# Script PowerShell para preparar os diretórios necessários para o docker-compose

Write-Host "Criando estrutura de diretórios em ~/credentials..." -ForegroundColor Green

# Obter o diretório home do usuário
$homeDir = $env:USERPROFILE
$credentialsDir = Join-Path $homeDir "credentials"

# Criar diretório base
New-Item -ItemType Directory -Force -Path $credentialsDir | Out-Null

# Criar subdiretórios para volumes
$directories = @(
    "mysql-data",
    "mysql-init", 
    "backend-logs",
    "nginx-conf",
    "nginx-logs"
)

foreach ($dir in $directories) {
    $fullPath = Join-Path $credentialsDir $dir
    New-Item -ItemType Directory -Force -Path $fullPath | Out-Null
    Write-Host "  Criado: $fullPath" -ForegroundColor Yellow
}

# Copiar configuração do nginx
Write-Host "Copiando configuração do Nginx..." -ForegroundColor Green
$sourceNginx = "nginx.conf"
$destNginx = Join-Path $credentialsDir "nginx-conf\default.conf"

if (Test-Path $sourceNginx) {
    Copy-Item $sourceNginx $destNginx -Force
    Write-Host "  Configuração do Nginx copiada para: $destNginx" -ForegroundColor Yellow
} else {
    Write-Host "  Aviso: nginx.conf não encontrado no diretório atual" -ForegroundColor Red
}

Write-Host ""
Write-Host "Estrutura de diretórios criada com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "Diretórios criados:" -ForegroundColor Cyan
Write-Host "  $credentialsDir\mysql-data     - Dados do MySQL" -ForegroundColor White
Write-Host "  $credentialsDir\mysql-init     - Scripts de inicialização do MySQL" -ForegroundColor White
Write-Host "  $credentialsDir\backend-logs   - Logs do backend Spring Boot" -ForegroundColor White
Write-Host "  $credentialsDir\nginx-conf     - Configurações do Nginx" -ForegroundColor White
Write-Host "  $credentialsDir\nginx-logs     - Logs do Nginx" -ForegroundColor White
Write-Host ""
Write-Host "Para iniciar a stack, execute:" -ForegroundColor Cyan
Write-Host "  docker-compose up -d" -ForegroundColor Yellow

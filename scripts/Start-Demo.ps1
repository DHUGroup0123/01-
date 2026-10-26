param([int]$Port = 28089)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $projectRoot
try {
    & .\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo' "-Dspring-boot.run.arguments=--server.port=$Port"
    if ($LASTEXITCODE -ne 0) { throw '演示环境启动失败，请查看上方错误。' }
} finally { Pop-Location }

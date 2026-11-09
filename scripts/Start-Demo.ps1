param([int]$Port=28089)
$ErrorActionPreference='Stop'
Push-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
try { & .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=$Port";if($LASTEXITCODE -ne 0){throw '启动失败，请检查 MySQL 服务、数据库是否存在及连接环境变量。'} } finally {Pop-Location}

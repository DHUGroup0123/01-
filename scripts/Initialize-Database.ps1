param(
 [string]$MySqlPath='C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe',
 [string]$DbHost='127.0.0.1',
 [int]$Port=3306,
 [string]$Username='root',
 [ValidatePattern('^[a-zA-Z0-9_]+$')][string]$Database='mall_01_full',
 [switch]$UseEnvironmentPassword
)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
$sql=Join-Path $projectRoot 'src\main\resources\mall_schema.sql'
$expected=((Get-Content -LiteralPath (Join-Path $projectRoot 'database\schema.sha256') -Raw).Split(' ')[0]).Trim()
if((Get-FileHash -LiteralPath $sql -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expected){throw 'SQL文件校验不符，停止执行。'}
$mysqlArgs=@('--no-defaults',"--host=$DbHost","--port=$Port",'--protocol=TCP',"--user=$Username",'--default-character-set=utf8mb4','--batch','--skip-column-names')
if($UseEnvironmentPassword){if(-not $env:MYSQL_PWD){throw '请先在本机设置MYSQL_PWD。'}}else{$mysqlArgs+='--password'}
& $MySqlPath @mysqlArgs "--execute=CREATE DATABASE IF NOT EXISTS $Database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if($LASTEXITCODE -ne 0){throw '无法创建或访问数据库。'}
$count=& $MySqlPath @mysqlArgs "--execute=SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$Database';"
if($LASTEXITCODE -ne 0){throw '无法确认数据库是否为空。'}
if([int]$count -ne 0){throw '目标库非空，初始化工具拒绝执行。请选择新的空库。'}
Push-Location -LiteralPath (Split-Path -Parent $sql)
try {
 & $MySqlPath @mysqlArgs "--database=$Database" '--execute=SOURCE mall_schema.sql;'
 if($LASTEXITCODE -ne 0){throw 'SQL导入失败。请检查错误，不要在已有数据的库中盲目重跑。'}
} finally {Pop-Location}
Write-Output "数据库 $Database 初始化完成，已校验初始化SQL。"

param(
    [string]$BaseUrl = 'http://localhost:28089',
    [ValidateSet('MySQL','H2 临时演示')][string]$ExpectedDatabase = 'MySQL'
)
$ErrorActionPreference = 'Stop'
$base = $BaseUrl.TrimEnd('/')
$result = Invoke-RestMethod -Uri "$base/api/environment" -TimeoutSec 15
if ($result.code -ne 200 -or $result.data.projectName -ne '01电商系统') { throw '项目身份检查失败' }
if ($result.data.springBootVersion -ne '4.0.8') { throw 'Spring Boot 版本不一致' }
if ($result.data.javaVersion -notmatch '^17[.]') { throw '请统一使用 Java 17' }
if ($result.data.schemaVersion -ne 'V001') { throw '数据结构版本不一致' }
if ($result.data.databaseMode -ne $ExpectedDatabase) { throw '数据库模式与预期不一致' }
if ($result.data.categoryCount -lt 3 -or $result.data.productCount -lt 3) { throw '初始化数据不完整' }
foreach ($route in @('/', '/admin', '/login')) {
    $page = Invoke-WebRequest -Uri "$base$route" -UseBasicParsing -TimeoutSec 15
    if ($page.StatusCode -ne 200 -or $page.Content -notmatch '01电商系统') { throw "页面检查失败：$route" }
}
$db = Invoke-RestMethod -Uri "$base/test-db" -TimeoutSec 15
if ($db.code -ne 200 -or $db.data -ne 1) { throw '数据库查询检查失败' }
$result.data | Format-List
Write-Output '01电商系统：本机环境检查通过。请将本机结果填写到三机验收表。'

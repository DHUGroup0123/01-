param([string]$BaseUrl='http://localhost:28089')
$ErrorActionPreference='Stop'
$result=Invoke-RestMethod -Uri ($BaseUrl.TrimEnd('/')+'/api/environment')
if($result.code -ne 200 -or $result.data.projectName -ne '01电商系统'){throw '项目检查失败'}
if($result.data.springBootVersion -ne '4.0.8' -or $result.data.javaVersion -notmatch '^17[.]'){throw '开发版本不一致'}
if($result.data.schemaVersion -ne 'MALL-01'){throw '数据库版本不一致'}
$result.data|Format-List

# 01电商系统

Spring Boot 4.0.8 / Java 17 / MyBatis 4.0.1 / Thymeleaf / MySQL 8.0。

包含商城首页、搜索、商品详情、用户注册登录、个人资料、收货信息、购物车、下单、支付演示、订单查询与确认收货，以及后台分类、商品、图片上传、轮播、首页配置、会员和订单管理。

成员A的用户与商城代码继续位于 `memberA`，在前两周实现上适配数据库与完整业务链路。页面沿用浅色与绿色主题。后台、购物车和订单代码与A模块通过Session及现有表字段衔接。

## 本地运行

1. 安装并启动 MySQL 8.0，使用 JDK 17。在 MySQL 客户端创建空库，数据库名可自行选择。

   ```sql
   CREATE DATABASE mall_01_full CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 在 IntelliJ IDEA 打开 `pom.xml`，将 Project SDK 和 Maven Runner JRE 设为 17。在 **Run → Edit Configurations → TeamWorkApplication → Environment variables** 添加以下变量。MySQL 账号需有该库的建表及读写权限。

   | 环境变量 | 示例 | 用途 |
   | --- | --- | --- |
   | `DB_NAME` | `mall_01_full` | 已创建的 MySQL 数据库名 |
   | `DB_USERNAME` | `root` | 本机 MySQL 用户名 |
   | `DB_PASSWORD` | 填写实际密码 | 本机 MySQL 密码 |
   | `DB_HOST`、`DB_PORT` | `localhost`、`3306` | 可选，默认值如示例 |

3. 运行 `TeamWorkApplication`。连接到空库时，应用会自动执行 `src/main/resources/mall_schema.sql`。已有表时跳过初始化。商城入口为 http://localhost:28089/，后台入口为 http://localhost:28089/admin。

也可从项目根目录通过 PowerShell 启动。在同一个终端中设置变量并运行以下命令。

```powershell
$env:DB_NAME='mall_01_full'
$env:DB_USERNAME='root'
$password=Read-Host 'MySQL密码' -AsSecureString
$ptr=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
try{$env:DB_PASSWORD=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)}finally{[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)}
.\mvnw.cmd spring-boot:run
```

启动已打包的 JAR 时，使用相同的环境变量，再在同一个终端执行 `java -jar .\运行包\01-ecommerce-system-1.0.0.jar`。如需指定完整 JDBC 地址，可设置 `DB_URL`，它优先于 `DB_HOST`、`DB_PORT` 和 `DB_NAME`。

后台初始账号admin，初始密码123456。用户可以注册手机号账号。支付仅为演示，不连接真实支付渠道。请只使用课程开发数据；初始化账号和MD5字段是数据库原有兼容格式。

## 数据与文件

- 初始化 SQL 包含 9 张 `tb_mall_` 表和演示数据。应用自动导入空库，也可使用 `scripts/Initialize-Database.ps1` 手动导入。
- 现有数据库使用旧表名时，请创建空库并通过 `DB_NAME` 选择。旧业务数据需另行迁移。
- 收货信息按表中 address 字段保存为一条 100 字以内的完整信息。
- 登录账号为11位手机号，昵称和个性签名可以编辑。上架值为0、下架为1，订单状态使用0/1/2/3/4及负数关闭值。
- 上传图片在uploads目录，可通过MALL_UPLOAD_DIR改到指定本地目录。不会写入SQL文件或打包目录。
- A模块保留的首页与账户页面使用已有风格；购物车、订单与后台按同一主题统一。

## 验证

代码集成完成后才进行构建及最终测试。测试必须使用专门空库，严禁指向有业务数据的数据库。

运行说明、最终测试记录和本地保存状态见docs/final目录。历史docs/week1及week2仅为原阶段记录，当前运行方式以本说明为准。

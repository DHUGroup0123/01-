# 01电商系统

工程项目管理课程独立实践项目。Spring Boot 4.0.8 / Java 17 / MyBatis 4.0.1 / Thymeleaf / MySQL 8.0。页面、数据库与业务代码在现有课程工程中自行实现，仅参考商城项目的功能划分。

## 成员 A 第二周成果

- 用户注册、图片验证码登录、退出登录；密码采用 PBKDF2 加盐哈希。
- Session 登录保护与禁用状态复核，CSRF 校验和登录频率限制。
- 个人资料更新，收货地址增删改查、默认地址管理、归属校验。
- 首页轮播、三级分类导航、热销、新品、推荐商品。
- 商品关键词搜索、分类筛选、价格排序、分页和详情。
- 实际测试记录、运行及协作说明、浏览器截图。

本次仅更新 `feature/member-a`，没有合并其他分支。后台商品维护归成员 B，购物车与交易归成员 C；本分支的临时后台入口仍为环境检查页，商品详情不会假装已经接通加购或支付。

## 快速体验

在 IDEA 或 PowerShell 中确认 JDK 17，项目根目录运行：

```powershell
.\mvnw.cmd -B verify
.\scripts\Start-Demo.ps1
```

访问 http://localhost:28089/ ，点击注册创建账号；没有内置固定密码账号。H2 演示数据在应用退出后清空。

日常开发使用 MySQL，首次建 `mall_01`、配置 DB_USERNAME / DB_PASSWORD，再启用 init。可选 sample 模式额外添加12件合成演示商品和2张自绘轮播图。详见 [第二周运行与演示说明](docs/week2/01-运行与演示说明.md)。

## 文档

| 入口 | 内容 |
| --- | --- |
| [第二周交付说明](docs/week2/00-第二周交付说明.md) | 功能完成情况与职责边界 |
| [运行与演示说明](docs/week2/01-运行与演示说明.md) | H2 / MySQL 启动、IDEA配置、演示脚本 |
| [接口与协作交接](docs/week2/02-接口与协作交接.md) | 已实现接口、CSRF、Session、字段与状态 |
| [测试与验收记录](docs/week2/03-测试与验收记录.md) | 自动测试、真实MySQL测试与浏览器结果 |
| [第一周历史文档](docs/week1/00-第一周交付说明.md) | V001数据字典、需求和原始阶段记录 |

## 结构

```text
src/main/java/com/team/team_work/
  common/                 统一返回与 Session 键
  config/                 登录与 CSRF 保护
  memberA/user/           用户、资料与收货地址
  memberA/mall/           商城首页、搜索、详情
  memberB/                成员 B 原目录，本次未修改
  memberC/                成员 C 原目录，本次未修改
src/main/resources/
  db/V001__schema.sql     第一周表结构，保持不变
  db/V001__seed.sql       第一周基线数据，保持不变
  db/V002__mall_demo.sql  第二周可选演示数据，无结构变更
  mapper/memberA/         MyBatis XML
  templates/memberA/mall/ 商城及用户页面
  static/                本地 CSS / JS / 自绘 SVG
```

Maven artifactId 为 `01-ecommerce-system`，数据库标识为 `mall_01`，原包路径与启动类保留，以兼容既有 IDEA 配置。项目显示名称统一为“01电商系统”。

# 01电商系统

工程项目管理课程项目。采用 Spring Boot 4.0.8、Java 17、Thymeleaf、MyBatis 4.0.1 和 MySQL 8.0，按成员 A、B、C 分工独立开发。

## 本次交付

成员 A 第一周：环境基线、数据库初始化、需求与任务清单、表关系与字段说明、协作接口草案、前后台启动入口以及验证工具。注册登录、搜索和完整后台业务属于第二周，本次不将占位页计为已完成业务。

只上传到 `feature/member-a`，不更新其他分支，不自动合并到 main。

## 快速运行

确保 `JAVA_HOME` 指向 JDK 17。在项目根目录运行：

```powershell
.\mvnw.cmd -B test
.\scripts\Start-Demo.ps1
```

打开 http://localhost:28089 和 http://localhost:28089/admin 。这是显式选择的 H2 临时演示环境，数据不持久化；正式开发使用 MySQL，步骤见 [运行说明](docs/week1/01-环境与运行说明.md)。

## 第一周成果索引

| 文件 | 用途 |
| --- | --- |
| [第一周交付说明](docs/week1/00-第一周交付说明.md) | 范围、交付物、完成与待验收项 |
| [环境与运行说明](docs/week1/01-环境与运行说明.md) | Windows、IDEA、Java 17、MySQL 初始化及排错 |
| [需求与任务清单](docs/week1/02-需求与任务清单.md) | 需求编号、成员职责、周计划与验收点 |
| [数据关系与字段](docs/week1/03-数据关系与字段说明.md) | V001 表关系、字段、约束、状态语义 |
| [协作接口约定](docs/week1/04-协作接口约定.md) | A/B/C 接口、Session、金额、库存、订单规则 |
| [测试与验收记录](docs/week1/05-测试与验收记录.md) | 已执行验证及三机验收表 |
| [进度与风险记录](docs/week1/06-进度与风险记录.md) | 周内任务、同步议题、版本差异 |
| [参考项目说明](docs/week1/07-参考项目说明.md) | 参考边界、版本适配与来源 |

## 目录

```text
src/main/java/com/team/team_work/
  common/                  公共返回结构与 Session 键
  config/                  公共配置
  memberA/user/            用户与个人中心
  memberA/mall/            商城与本周环境入口
  memberB/product/         商品与分类
  memberB/admin/           后台运营
  memberC/cart/            购物车
  memberC/order/           订单与交易
src/main/resources/
  db/V001__schema.sql      数据结构唯一来源
  db/V001__seed.sql        可重复执行的演示数据
  mapper/memberA/          本周 MyBatis XML
  templates/memberA/mall/  前台及临时后台启动页
  templates/memberB/admin/ 后台业务模板预留
  templates/memberC/order/ 交易模板预留
```

页面、文档与 Maven 显示名称统一为“01电商系统”。`com.team.team_work` 包路径保留以兼容现有目录；Maven artifactId 为 `01-ecommerce-system`，数据库标识为 `mall_01`，这些是技术标识。

# 成员 B 商品与后台运营

提交目标为组仓库的 `feature/member-b` 分支。参考组长指定的 newbee-mall，沿用组仓库已有 Java 17、Spring Boot 4.0.8、MyBatis 4.0.1、Thymeleaf 环境。没有修改根 `pom.xml`、`application.properties`、公共包或 A/C 模块。任务书中的 JDK 8 / Boot 2.7.5 与组仓库不同，本次遵照“不要改公共配置”的要求，以组仓库配置为准。

后台入口：`http://localhost:28089/admin/b`。

## 已交付功能

| 范围 | 实现 |
| --- | --- |
| 管理员 | 图形验证码、登录、退出、昵称修改、原密码校验后改密、会话过期、改密使其他会话失效 |
| 分类 | 三级树形展示、新增、重命名、排序、删除；同级重名、超过三级、移动层级、存在子分类或商品时拒绝 |
| 商品 | 新增、编辑、关键词/分类/状态过滤、分页、封面/轮播地址、详情、上下架、价格与库存校验、编辑版本检查 |
| 图片 | PNG/JPEG/GIF，最大5MB，真实格式与尺寸校验、重编码PNG、内容散列命名；相同内容重复上传不产生多个文件 |
| 首页 | 轮播图新增/编辑/排序/上下线/删除；热销、新品、推荐商品配置与启停 |
| 会员 | 账号/昵称查询、分页、禁用/解禁；不返回密码字段 |
| 联调 | 商品服务、会员状态服务、按订单明细业务编号去重的库存预占/归还服务 |

页面采用本地 Bootstrap / AdminLTE 样式、jQuery、SweetAlert 与 wangEditor，不依赖外部 CDN。列表使用普通 Bootstrap 表格与服务端分页，未引入 jqGrid。商品详情按纯文本存储和显示，wangEditor 用于文本编辑；不保存或执行任意富文本 HTML。没有给公共 POM 增加 Hutool 依赖，图形验证码由 JDK 图形 API 生成。

## 文件位置

- `src/main/java/com/team/team_work/memberB/product/`：分类、商品、库存的 Controller、Service、Mapper、Entity。
- `src/main/java/com/team/team_work/memberB/admin/`：管理员、后台运营、会员、上传、会话校验、异常与去重。
- `src/main/resources/com/team/team_work/memberB/`：与 Mapper 同名的 MyBatis XML。MyBatis 由接口路径加载 XML，无须改公共 mapper-locations 配置。
- `src/main/resources/templates/memberB/admin/`：登录及后台页面。
- `src/main/resources/static/memberB/`：本模块脚本、样式、第三方前端资源。
- `src/main/resources/memberB/`：本模块默认参数与数据库脚本。
- `src/test/java/com/team/team_work/memberB/`：集成测试与并发测试。
- `memberB/`：部署、联调、测试记录和 B 专用验证 POM。

## MySQL 8 部署

1. 安装组仓库所需的 JDK 17、Maven 3.x 和 MySQL 8。创建一个专用开发数据库（公共配置默认 `team_work_db`）。若数据库已有 A/C 数据，先核对下述表契约，先备份，再由组内确认初始化方案。本模块不会在默认启动时建表、清库或修改旧表。
2. 在新数据库执行 `src/main/resources/memberB/schema.sql`。也可在首次启动时显式传入 `--memberb.initialize=true`，由初始化器执行同一脚本。脚本只含 `CREATE TABLE IF NOT EXISTS` 和去重锁初始化；没有 `DROP` 或现存表的 `ALTER`。
3. 在启动进程环境设置 `DB_USERNAME`、`DB_PASSWORD`；首次初始化另设 `MEMBERB_ADMIN_NAME`（缺省 `admin`）和 `MEMBERB_ADMIN_PASSWORD`（12至128字符）。没有内置正式管理员密码。密码以 PBKDF2-HMAC-SHA256、随机盐、210000次迭代保存，不记录明文。
4. 设置 `MEMBERB_UPLOAD_DIR` 为绝对且可写的持久目录。上传目录应与源码、静态资源发布目录分开。该目录通过 `/member-b/media/` 提供图片访问。
5. 在项目根目录执行 `mvn -DskipTests package`，再启动：

```text
java -jar target/team-work-0.0.1-SNAPSHOT.jar --memberb.initialize=true --memberb.seed=true --spring.servlet.multipart.max-file-size=5MB --spring.servlet.multipart.max-request-size=6MB
```

以上两个初始化参数仅在首次建立开发演示库时使用。随后去掉它们正常启动。管理员已存在时初始化器不重置密码；演示分类已存在时不重复插入演示商品。`memberb.seed=true` 会创建三级演示分类、4个商品、热销/新品/推荐各1个配置、轮播图和演示会员，并在上传目录生成一张测试图片。演示图片和种子不包含用户提供的任务书。

不要把密码写入命令参数、提交到 Git 或放入前端。推荐通过 IDE 的进程环境或部署系统的秘密变量设置。正式部署使用 HTTPS、仅保留必要端口、配置安全会话 Cookie，并备份数据库和上传目录。以上是本模块部署要求，不会擅自更改团队公共配置。

## B 专用本地演示和验证

为了保持公共 POM 不动，`memberB/pom.xml` 是独立验证入口：使用与组仓库相同的依赖版本和同一套源代码，仅额外包含 H2。它不是新的业务实现，也不改变根工程。

```text
mvn -f memberB/pom.xml -Dspring.profiles.active=memberb-test test
mvn -f memberB/pom.xml -DskipTests package
java -jar memberB/target/member-b-verification-0.0.1-SNAPSHOT.jar --spring.profiles.active=memberb-demo
```

演示也需要预先设置 `MEMBERB_ADMIN_PASSWORD`。演示 profile 仅监听 `127.0.0.1`，数据保存在启动目录的 `member-b-data/`。它用于 B 模块独立演示，不替代组内 MySQL 联调。

## 后台操作

1. 登录后台，输入管理员账号、密码及图形验证码。
2. 分类管理中新建一级、二级、三级分类。已有分类只支持更名与排序，避免无意改变商品层级。
3. 新增商品，选三级分类，上传图片，填写原价、售价、库存和详情。新商品默认下架，保存后单独点击“上架”。
4. 首页配置中选择在售且有库存的商品，配置热销/新品/推荐。轮播页可上传轮播图片并设置站内跳转路径、排序和上线状态。
5. 下架商品前先停用或删除它的首页配置。存在子分类或商品关联的分类不可删除。
6. 会员页查询后进行禁用/解禁；个人设置页修改昵称或密码，改密后重新登录。

## 交付边界

B 后台及数据库行为已经独立验证。A/C 当前分支的前台、注册与交易逻辑不在本次修改范围，完整“浏览—下单—支付—出库—收货”仍需组内联调。没有伪造 A/C 功能或宣称该完整交易链路已验证。详见 [接口与数据约定](接口与数据约定.md) 和 [测试记录](测试记录.md)。

# 社区服务系统后端（阶段1-2）

## 1. 项目说明
本项目为毕业设计后端工程，采用 Spring Boot 3 + MyBatis + MySQL + Redis。  
当前已完成阶段1-2：项目骨架、认证授权、用户角色权限、组织管理、数据范围、日志审计、SQL 初始化、技术文档。

## 2. 运行前准备
- JDK 17+
- Maven 3.9+
- MySQL 8.x
- Redis 6.x+

## 3. 初始化步骤
在 MySQL 中按顺序执行：
1. `sql/01_schema.sql`
2. `sql/02_index.sql`
3. `sql/03_init_data.sql`

## 4. 启动方式
```bash
mvn clean spring-boot:run
```

## 5. 默认账号
- 用户名：`admin`
- 密码：`Admin@123456`
- 首登改密：是

## 6. 文档路径
- 设计文档：`docs/`
- 升级说明：`sql/04_upgrade_notes.md`
- 在线接口：`/swagger-ui/index.html` 与 `/doc.html`


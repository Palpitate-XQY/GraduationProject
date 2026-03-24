# 社区服务系统后端

## 1. 项目说明
本项目为“基于 Spring Boot 3 的社区服务系统后端”，当前已完成阶段1-5：
- 基础工程骨架与统一异常响应
- 认证授权（Spring Security + JWT）
- 用户角色权限与数据范围控制
- 组织模型与小区-物业服务关系
- 公告、活动、报修核心业务
- 登录日志与操作日志查询
- 居民注册与忘记密码（验证码重置）

## 2. 技术栈
- JDK 17
- Spring Boot 3.x
- Spring Security
- MyBatis（XML Mapper）
- MySQL 8.x
- Redis
- SpringDoc + Knife4j

## 3. 目录结构
```text
back/community
  ├─src/main/java/xxqqyyy/community
  │  ├─common
  │  ├─config
  │  ├─security
  │  ├─infrastructure
  │  └─modules
  │     ├─auth
  │     ├─system
  │     ├─org
  │     ├─notice
  │     ├─activity
  │     ├─repair
  │     └─log
  ├─src/main/resources
  │  ├─application*.yml
  │  └─mapper
  ├─sql
  │  ├─01_schema.sql
  │  ├─02_index.sql
  │  ├─03_init_data.sql
  │  └─04_upgrade_notes.md
  └─docs
     ├─01-项目概述.md
     ├─...
     └─10-开发里程碑与提交说明.md
```

## 4. 初始化数据库
在 MySQL 手动按顺序执行：
1. `sql/01_schema.sql`
2. `sql/02_index.sql`
3. `sql/03_init_data.sql`

## 5. 启动方式
```bash
mvn clean spring-boot:run
```

## 6. 接口文档
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- Knife4j：`http://localhost:8080/doc.html`

## 7. 默认管理员
- 用户名：`admin`
- 初始密码：`Admin@123456`
- 首登改密：`must_change_password=1`

## 8. 居民示例账号
- 用户名：`resident_demo`
- 初始密码：`Admin@123456`
- 角色：`RESIDENT`

## 9. 认证接口补充
- `POST /api/auth/resident/register`
- `POST /api/auth/password/reset-code`
- `POST /api/auth/password/reset-by-code`

## 10. 测试
```bash
mvn test
```

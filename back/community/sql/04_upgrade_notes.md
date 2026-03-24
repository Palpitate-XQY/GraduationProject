# 数据库升级说明（阶段2）

## 当前版本说明
- 当前脚本覆盖阶段1-2所需的系统表、组织表、基础业务关联表、日志表。
- 采用“无外键 + 应用层校验与事务”策略，避免跨模块迁移时的强耦合阻塞。

## 后续阶段扩展计划
- 阶段3（公告/活动）：新增 `biz_notice`、`biz_notice_scope`、`biz_activity`、`biz_activity_scope`、`biz_activity_signup`。
- 阶段4（报修）：新增 `biz_repair_order`、`biz_repair_order_log`、`biz_repair_attachment`。
- 阶段5（优化）：按慢 SQL 和业务检索路径补充组合索引与归档策略。

## 升级约束
- 升级脚本仍保持手工执行，不强依赖 Flyway/Liquibase。
- 所有新增表继续遵循：`InnoDB + utf8mb4 + 中文注释 + 逻辑删除 + 审计字段 + 无外键`。
- 业务关联一致性继续由服务层事务、状态校验、越权校验保证。


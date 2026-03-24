# 数据库升级说明（阶段5）

## 当前版本说明
- 当前脚本已覆盖阶段1-5：系统权限、组织、公告、活动、报修、日志与基础业务关联表。
- 采用“无外键 + 应用层校验与事务”策略，避免跨模块迁移时的强耦合阻塞。

## 阶段4（已实现）
- 新增报修核心表：`biz_repair_order`、`biz_repair_order_log`、`biz_repair_attachment`。
- 新增报修索引：状态维度、小区维度、物业维度、维修员维度、居民维度与日志查询维度索引。
- 初始化报修权限点：`repair:*` 全量接口权限。
- 初始化报修示例数据：示例工单与完整流转日志样例。

## 阶段5（已实现）
- 新增日志查询权限：`log:login:list`、`log:operation:list`。
- 新增日志查询索引：`idx_log_login_ip_time`、`idx_log_operation_success_time`。
- 初始化脚本增加超级管理员权限兜底同步，避免增量权限漏授。
- 初始化脚本补充居民角色 `RESIDENT`、居民示例账号与居民档案样例，支持居民端全流程联调。

## 阶段5（附件能力增强）
- 新增文件存储配置表：`sys_storage_config`，支持本地存储与七牛云存储切换。
- 补充文件与存储权限：`file:upload`、`file:view`、`sys:storage:view`、`sys:storage:update`。
- 调整 `biz_file_info.storage_type` 枚举说明为 `LOCAL/QINIU`。

## 升级约束
- 升级脚本仍保持手工执行，不强依赖 Flyway/Liquibase。
- 所有新增表继续遵循：`InnoDB + utf8mb4 + 中文注释 + 逻辑删除 + 审计字段 + 无外键`。
- 业务关联一致性继续由服务层事务、状态校验、越权校验保证。

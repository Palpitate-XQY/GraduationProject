# 管理端后端契约对照矩阵

> 版本：本轮全量收口（公告/活动/报修/系统/组织/日志/存储/居民/字典）

## 1. 模块与接口

| 模块 | 后端接口前缀 | 前端 API 模块 | 管理端页面 |
| --- | --- | --- | --- |
| 公告 | `/api/notices` | `src/api/notice.ts` | `AdminNoticeView.vue` |
| 活动 | `/api/activities` | `src/api/activity.ts` | `AdminActivityView.vue` |
| 报修 | `/api/repairs` | `src/api/repair.ts` | `AdminRepairView.vue` |
| 用户/角色/权限 | `/api/system/users|roles|permissions` | `src/api/system.ts` | `AdminUsers/Role/PermissionsView.vue` |
| 组织/关联 | `/api/org` | `src/api/org.ts` | `AdminOrgTree/ComplexPropertyRelView.vue` |
| 日志 | `/api/logs` | `src/api/log.ts` | `AdminLoginLogs/OperationLogsView.vue` |
| 存储 | `/api/system/storage-config` | `src/api/storage.ts` | `AdminStorageView.vue` |
| 居民档案 | `/api/resident/profiles` | `src/api/resident-admin.ts` | `AdminResidentView.vue` |
| 字典 | `/api/system/dicts` | `src/api/dict.ts` | `AdminDictView.vue` |
| 文件上传 | `/api/files` | `src/api/file.ts` | `AdminFilePicker.vue` |

## 2. 关键契约约束

| 领域 | 约束 |
| --- | --- |
| 公告类型 | `STREET_COMMUNITY` / `PROPERTY` |
| 公告/活动范围 | `scopeItems` 必须含 `scopeType + scopeRefId` |
| 活动范围 | 不允许 `scopeType=SELF` |
| 活动时间窗 | `signupStart<=signupEnd<=activityEnd` 且 `activityStart<=activityEnd` |
| 报修动作字段 | `reject.reason` / `process.processDesc` / `submit.finishDesc` / `reopen.feedback` / `evaluate.score` |
| 查询时间格式 | 日志与报修管理查询使用 `yyyy-MM-dd HH:mm:ss` |
| 字典字段 | `dictCode/dictName/status`、`dictTypeCode/dictLabel/dictValue/sort/status` |
| 权限 | 路由、菜单、按钮统一 `permission_code` 判定 |

## 3. 校验策略

- 前端强校验先于请求提交，避免缺字段到后端才失败。
- 字段级错误优先展示在表单附近；全局错误由请求层兜底提示。
- 403 仅阻断当前动作，不影响页面浏览与其他模块操作。

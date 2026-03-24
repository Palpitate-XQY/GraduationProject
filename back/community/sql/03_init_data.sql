-- 社区服务系统数据库初始化脚本（基础数据）
SET NAMES utf8mb4;

-- ========== 组织初始化 ==========
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_type, status, sort, ancestor_path, create_by, update_by, deleted, version)
VALUES
    (1, NULL, 'STREET_WANLI', '湾里街道', 'STREET', 1, 1, '/', 1, 1, 0, 0),
    (2, 1, 'COMMUNITY_HARMONY', '和谐社区', 'COMMUNITY', 1, 1, '/1/', 1, 1, 0, 0),
    (3, 2, 'COMPLEX_HAPPY_HOME', '幸福家园小区', 'COMPLEX', 1, 1, '/1/2/', 1, 1, 0, 0),
    (4, 1, 'PROPERTY_XINGFU', '幸福物业公司', 'PROPERTY_COMPANY', 1, 2, '/1/', 1, 1, 0, 0),
    (5, 4, 'PROPERTY_SERVICE_DEPT', '物业客服部', 'DEPARTMENT', 1, 1, '/1/4/', 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    org_name = VALUES(org_name),
    org_type = VALUES(org_type),
    status = VALUES(status),
    sort = VALUES(sort),
    ancestor_path = VALUES(ancestor_path),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

-- ========== 角色初始化 ==========
INSERT INTO sys_role (id, role_code, role_name, status, parent_role_id, allow_create_child, sort, remark, create_by, update_by, deleted, version)
VALUES
    (1, 'SUPER_ADMIN', '超级管理员', 1, NULL, 1, 1, '系统最高权限角色', 1, 1, 0, 0),
    (2, 'STREET_ADMIN', '街道管理员', 1, 1, 1, 2, '街道层级管理角色', 1, 1, 0, 0),
    (3, 'COMMUNITY_ADMIN', '社区管理员', 1, 2, 1, 3, '社区层级管理角色', 1, 1, 0, 0),
    (4, 'PROPERTY_ADMIN', '物业管理员', 1, 2, 1, 4, '物业公司管理角色', 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    status = VALUES(status),
    parent_role_id = VALUES(parent_role_id),
    allow_create_child = VALUES(allow_create_child),
    sort = VALUES(sort),
    remark = VALUES(remark),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

-- ========== 权限初始化 ==========
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, method, sort, create_by, update_by, deleted, version)
VALUES
    (1001, 'sys:user:list', '用户列表', 'API', NULL, '/api/system/users', 'GET', 1, 1, 1, 0, 0),
    (1002, 'sys:user:view', '用户详情', 'API', NULL, '/api/system/users/{id}', 'GET', 2, 1, 1, 0, 0),
    (1003, 'sys:user:create', '用户新增', 'API', NULL, '/api/system/users', 'POST', 3, 1, 1, 0, 0),
    (1004, 'sys:user:update', '用户编辑', 'API', NULL, '/api/system/users', 'PUT', 4, 1, 1, 0, 0),
    (1005, 'sys:user:delete', '用户删除', 'API', NULL, '/api/system/users/{id}', 'DELETE', 5, 1, 1, 0, 0),
    (1006, 'sys:user:assign-role', '用户分配角色', 'API', NULL, '/api/system/users/roles', 'PUT', 6, 1, 1, 0, 0),
    (1101, 'sys:role:list', '角色列表', 'API', NULL, '/api/system/roles', 'GET', 11, 1, 1, 0, 0),
    (1102, 'sys:role:view', '角色详情', 'API', NULL, '/api/system/roles/{id}', 'GET', 12, 1, 1, 0, 0),
    (1103, 'sys:role:create', '角色新增', 'API', NULL, '/api/system/roles', 'POST', 13, 1, 1, 0, 0),
    (1104, 'sys:role:update', '角色编辑', 'API', NULL, '/api/system/roles', 'PUT', 14, 1, 1, 0, 0),
    (1105, 'sys:role:delete', '角色删除', 'API', NULL, '/api/system/roles/{id}', 'DELETE', 15, 1, 1, 0, 0),
    (1201, 'sys:permission:list', '权限列表', 'API', NULL, '/api/system/permissions', 'GET', 21, 1, 1, 0, 0),
    (1202, 'sys:permission:view', '权限详情', 'API', NULL, '/api/system/permissions/{id}', 'GET', 22, 1, 1, 0, 0),
    (1203, 'sys:permission:create', '权限新增', 'API', NULL, '/api/system/permissions', 'POST', 23, 1, 1, 0, 0),
    (1204, 'sys:permission:update', '权限编辑', 'API', NULL, '/api/system/permissions', 'PUT', 24, 1, 1, 0, 0),
    (1205, 'sys:permission:delete', '权限删除', 'API', NULL, '/api/system/permissions/{id}', 'DELETE', 25, 1, 1, 0, 0),
    (1301, 'org:tree:view', '组织树查看', 'API', NULL, '/api/org/tree', 'GET', 31, 1, 1, 0, 0),
    (1302, 'org:create', '组织新增', 'API', NULL, '/api/org', 'POST', 32, 1, 1, 0, 0),
    (1303, 'org:update', '组织编辑', 'API', NULL, '/api/org', 'PUT', 33, 1, 1, 0, 0),
    (1304, 'org:delete', '组织删除', 'API', NULL, '/api/org/{id}', 'DELETE', 34, 1, 1, 0, 0),
    (1305, 'org:complex-property:list', '小区物业关联列表', 'API', NULL, '/api/org/complex-property-rel', 'GET', 35, 1, 1, 0, 0),
    (1306, 'org:complex-property:create', '小区物业关联新增', 'API', NULL, '/api/org/complex-property-rel', 'POST', 36, 1, 1, 0, 0),
    (1307, 'org:complex-property:delete', '小区物业关联删除', 'API', NULL, '/api/org/complex-property-rel/{id}', 'DELETE', 37, 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    permission_name = VALUES(permission_name),
    permission_type = VALUES(permission_type),
    parent_id = VALUES(parent_id),
    path = VALUES(path),
    method = VALUES(method),
    sort = VALUES(sort),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

-- ========== 管理员账号（默认密码：Admin@123456） ==========
INSERT INTO sys_user (id, username, password, nickname, phone, email, status, org_id, must_change_password, create_by, update_by, deleted, version)
VALUES
    (1, 'admin', '$2b$12$y7U3AmqkqckZck4tFE31Ye.FnEia77IqShzAEzutD3OgmWPJege9y', '系统管理员', '13800000000', 'admin@community.local', 1, 1, 1, 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    nickname = VALUES(nickname),
    phone = VALUES(phone),
    email = VALUES(email),
    status = VALUES(status),
    org_id = VALUES(org_id),
    must_change_password = VALUES(must_change_password),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

-- ========== 角色权限、用户角色 ==========
DELETE FROM sys_user_role WHERE user_id = 1;
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

DELETE FROM sys_role_permission WHERE role_id = 1;
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE deleted = 0;

-- 街道管理员权限示例
DELETE FROM sys_role_permission WHERE role_id = 2;
INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (2, 1001), (2, 1002), (2, 1003), (2, 1004), (2, 1006),
    (2, 1101), (2, 1102), (2, 1103), (2, 1104),
    (2, 1201), (2, 1202),
    (2, 1301), (2, 1302), (2, 1303), (2, 1305), (2, 1306);

-- 社区管理员权限示例
DELETE FROM sys_role_permission WHERE role_id = 3;
INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (3, 1001), (3, 1002), (3, 1004),
    (3, 1101), (3, 1102),
    (3, 1201), (3, 1202),
    (3, 1301), (3, 1303), (3, 1305);

-- 物业管理员权限示例
DELETE FROM sys_role_permission WHERE role_id = 4;
INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (4, 1001), (4, 1002),
    (4, 1301), (4, 1305), (4, 1306);

-- ========== 数据范围 ==========
DELETE FROM sys_role_scope WHERE role_id IN (1, 2, 3, 4);
INSERT INTO sys_role_scope (role_id, scope_type, scope_ref_id) VALUES (1, 'ALL', NULL);
INSERT INTO sys_role_scope (role_id, scope_type, scope_ref_id) VALUES (2, 'STREET', 1);
INSERT INTO sys_role_scope (role_id, scope_type, scope_ref_id) VALUES (3, 'COMMUNITY', 2);
INSERT INTO sys_role_scope (role_id, scope_type, scope_ref_id) VALUES (4, 'PROPERTY_COMPANY', 4);

-- ========== 小区物业关系 ==========
INSERT INTO biz_complex_property_rel (id, complex_org_id, property_company_org_id, status, create_by, update_by, deleted, version)
VALUES (1, 3, 4, 1, 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

-- ========== 字典初始化 ==========
INSERT INTO sys_dict_type (id, dict_code, dict_name, status, create_by, update_by, deleted, version)
VALUES
    (1, 'user_status', '用户状态', 1, 1, 1, 0, 0),
    (2, 'org_type', '组织类型', 1, 1, 1, 0, 0),
    (3, 'data_scope_type', '数据范围类型', 1, 1, 1, 0, 0),
    (4, 'notice_status', '公告状态', 1, 1, 1, 0, 0),
    (5, 'activity_status', '活动状态', 1, 1, 1, 0, 0),
    (6, 'repair_status', '报修状态', 1, 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    dict_name = VALUES(dict_name),
    status = VALUES(status),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

DELETE FROM sys_dict_data WHERE dict_type_code IN ('user_status', 'org_type', 'data_scope_type', 'notice_status', 'activity_status', 'repair_status');
INSERT INTO sys_dict_data (dict_type_code, dict_label, dict_value, sort, status, create_by, update_by, deleted, version) VALUES
('user_status', '启用', '1', 1, 1, 1, 1, 0, 0),
('user_status', '禁用', '0', 2, 1, 1, 1, 0, 0),
('org_type', '街道', 'STREET', 1, 1, 1, 1, 0, 0),
('org_type', '社区', 'COMMUNITY', 2, 1, 1, 1, 0, 0),
('org_type', '小区', 'COMPLEX', 3, 1, 1, 1, 0, 0),
('org_type', '物业公司', 'PROPERTY_COMPANY', 4, 1, 1, 1, 0, 0),
('org_type', '部门', 'DEPARTMENT', 5, 1, 1, 1, 0, 0),
('data_scope_type', '全部数据', 'ALL', 1, 1, 1, 1, 0, 0),
('data_scope_type', '街道范围', 'STREET', 2, 1, 1, 1, 0, 0),
('data_scope_type', '社区范围', 'COMMUNITY', 3, 1, 1, 1, 0, 0),
('data_scope_type', '小区范围', 'COMPLEX', 4, 1, 1, 1, 0, 0),
('data_scope_type', '物业公司范围', 'PROPERTY_COMPANY', 5, 1, 1, 1, 0, 0),
('data_scope_type', '自定义范围', 'CUSTOM', 6, 1, 1, 1, 0, 0),
('data_scope_type', '仅本人', 'SELF', 7, 1, 1, 1, 0, 0),
('notice_status', '草稿', 'DRAFT', 1, 1, 1, 1, 0, 0),
('notice_status', '已发布', 'PUBLISHED', 2, 1, 1, 1, 0, 0),
('notice_status', '已撤回', 'RECALLED', 3, 1, 1, 1, 0, 0),
('activity_status', '草稿', 'DRAFT', 1, 1, 1, 1, 0, 0),
('activity_status', '报名中', 'REGISTRATION', 2, 1, 1, 1, 0, 0),
('activity_status', '已结束', 'FINISHED', 3, 1, 1, 1, 0, 0),
('repair_status', '待受理', 'PENDING_ACCEPT', 1, 1, 1, 1, 0, 0),
('repair_status', '已受理', 'ACCEPTED', 2, 1, 1, 1, 0, 0),
('repair_status', '已分派', 'ASSIGNED', 3, 1, 1, 1, 0, 0),
('repair_status', '处理中', 'PROCESSING', 4, 1, 1, 1, 0, 0),
('repair_status', '待确认', 'WAIT_CONFIRM', 5, 1, 1, 1, 0, 0),
('repair_status', '已完成', 'DONE', 6, 1, 1, 1, 0, 0),
('repair_status', '已关闭', 'CLOSED', 7, 1, 1, 1, 0, 0),
('repair_status', '已驳回', 'REJECTED', 8, 1, 1, 1, 0, 0),
('repair_status', '重新处理', 'REOPENED', 9, 1, 1, 1, 0, 0);


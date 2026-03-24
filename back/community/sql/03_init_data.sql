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
    (1301, 'org:tree:view', '组织树查询', 'API', NULL, '/api/org/tree', 'GET', 31, 1, 1, 0, 0),
    (1302, 'org:create', '组织新增', 'API', NULL, '/api/org', 'POST', 32, 1, 1, 0, 0),
    (1303, 'org:update', '组织编辑', 'API', NULL, '/api/org', 'PUT', 33, 1, 1, 0, 0),
    (1304, 'org:delete', '组织删除', 'API', NULL, '/api/org/{id}', 'DELETE', 34, 1, 1, 0, 0),
    (1305, 'org:complex-property:list', '小区物业关联列表', 'API', NULL, '/api/org/complex-property-rel', 'GET', 35, 1, 1, 0, 0),
    (1306, 'org:complex-property:create', '小区物业关联新增', 'API', NULL, '/api/org/complex-property-rel', 'POST', 36, 1, 1, 0, 0),
    (1307, 'org:complex-property:delete', '小区物业关联删除', 'API', NULL, '/api/org/complex-property-rel/{id}', 'DELETE', 37, 1, 1, 0, 0),
    (1401, 'notice:list', '公告列表', 'API', NULL, '/api/notices', 'GET', 41, 1, 1, 0, 0),
    (1402, 'notice:view', '公告详情', 'API', NULL, '/api/notices/{id}', 'GET', 42, 1, 1, 0, 0),
    (1403, 'notice:create', '公告新增', 'API', NULL, '/api/notices', 'POST', 43, 1, 1, 0, 0),
    (1404, 'notice:update', '公告编辑', 'API', NULL, '/api/notices', 'PUT', 44, 1, 1, 0, 0),
    (1405, 'notice:delete', '公告删除', 'API', NULL, '/api/notices/{id}', 'DELETE', 45, 1, 1, 0, 0),
    (1406, 'notice:publish', '公告发布', 'API', NULL, '/api/notices/{id}/publish', 'POST', 46, 1, 1, 0, 0),
    (1407, 'notice:recall', '公告撤回', 'API', NULL, '/api/notices/{id}/recall', 'POST', 47, 1, 1, 0, 0),
    (1408, 'notice:resident:list', '居民公告列表', 'API', NULL, '/api/notices/resident/page', 'GET', 48, 1, 1, 0, 0),
    (1409, 'notice:resident:view', '居民公告详情', 'API', NULL, '/api/notices/resident/{id}', 'GET', 49, 1, 1, 0, 0),
    (1501, 'activity:list', '活动列表', 'API', NULL, '/api/activities', 'GET', 51, 1, 1, 0, 0),
    (1502, 'activity:view', '活动详情', 'API', NULL, '/api/activities/{id}', 'GET', 52, 1, 1, 0, 0),
    (1503, 'activity:create', '活动新增', 'API', NULL, '/api/activities', 'POST', 53, 1, 1, 0, 0),
    (1504, 'activity:update', '活动编辑', 'API', NULL, '/api/activities', 'PUT', 54, 1, 1, 0, 0),
    (1505, 'activity:delete', '活动删除', 'API', NULL, '/api/activities/{id}', 'DELETE', 55, 1, 1, 0, 0),
    (1506, 'activity:publish', '活动发布', 'API', NULL, '/api/activities/{id}/publish', 'POST', 56, 1, 1, 0, 0),
    (1507, 'activity:recall', '活动撤回', 'API', NULL, '/api/activities/{id}/recall', 'POST', 57, 1, 1, 0, 0),
    (1508, 'activity:resident:list', '居民活动列表', 'API', NULL, '/api/activities/resident/page', 'GET', 58, 1, 1, 0, 0),
    (1509, 'activity:resident:view', '居民活动详情', 'API', NULL, '/api/activities/resident/{id}', 'GET', 59, 1, 1, 0, 0),
    (1510, 'activity:signup', '活动报名', 'API', NULL, '/api/activities/{id}/signup', 'POST', 60, 1, 1, 0, 0),
    (1511, 'activity:signup:list', '活动报名名单', 'API', NULL, '/api/activities/{id}/signups', 'GET', 61, 1, 1, 0, 0),
    (1512, 'activity:stats', '活动统计', 'API', NULL, '/api/activities/{id}/stats', 'GET', 62, 1, 1, 0, 0)
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

DELETE FROM sys_role_permission WHERE role_id = 2;
INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (2, 1001), (2, 1002), (2, 1003), (2, 1004), (2, 1006),
    (2, 1101), (2, 1102), (2, 1103), (2, 1104),
    (2, 1201), (2, 1202),
    (2, 1301), (2, 1302), (2, 1303), (2, 1305), (2, 1306),
    (2, 1401), (2, 1402), (2, 1403), (2, 1404), (2, 1406), (2, 1407), (2, 1408), (2, 1409),
    (2, 1501), (2, 1502), (2, 1503), (2, 1504), (2, 1506), (2, 1507), (2, 1508), (2, 1509), (2, 1511), (2, 1512);

DELETE FROM sys_role_permission WHERE role_id = 3;
INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (3, 1001), (3, 1002), (3, 1004),
    (3, 1301), (3, 1303), (3, 1305),
    (3, 1401), (3, 1402), (3, 1403), (3, 1404), (3, 1406), (3, 1407), (3, 1408), (3, 1409),
    (3, 1501), (3, 1502), (3, 1503), (3, 1504), (3, 1506), (3, 1507), (3, 1508), (3, 1509), (3, 1511), (3, 1512);

DELETE FROM sys_role_permission WHERE role_id = 4;
INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (4, 1001), (4, 1002),
    (4, 1301), (4, 1305), (4, 1306),
    (4, 1401), (4, 1402), (4, 1403), (4, 1404), (4, 1406), (4, 1407), (4, 1408), (4, 1409),
    (4, 1501), (4, 1502), (4, 1503), (4, 1504), (4, 1506), (4, 1507), (4, 1508), (4, 1509), (4, 1511), (4, 1512);

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

-- ========== 公告与活动示例数据 ==========
INSERT INTO biz_notice (id, notice_type, title, content, status, top_flag, publisher_org_id, publish_time, create_by, update_by, deleted, version)
VALUES
    (1, 'STREET_COMMUNITY', '街道春季卫生整治通知', '本周六将开展街道统一卫生整治，请居民做好配合。', 'PUBLISHED', 1, 1, NOW(), 1, 1, 0, 0),
    (2, 'PROPERTY', '幸福家园电梯巡检公告', '物业将于明日进行电梯巡检，期间部分时段暂停运行。', 'PUBLISHED', 0, 4, NOW(), 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    status = VALUES(status),
    top_flag = VALUES(top_flag),
    publisher_org_id = VALUES(publisher_org_id),
    publish_time = VALUES(publish_time),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

DELETE FROM biz_notice_scope WHERE notice_id IN (1, 2);
INSERT INTO biz_notice_scope (notice_id, scope_type, scope_ref_id)
VALUES
    (1, 'STREET', 1),
    (2, 'COMPLEX', 3);

INSERT INTO biz_activity (
    id, title, content, activity_start_time, activity_end_time, signup_start_time, signup_end_time,
    location, max_participants, status, publisher_org_id, publish_time, create_by, update_by, deleted, version
)
VALUES
    (1, '社区健康讲座', '邀请社区医生开展春季健康知识讲座。', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY) + INTERVAL 2 HOUR,
     NOW(), DATE_ADD(NOW(), INTERVAL 6 DAY), '和谐社区活动中心', 100, 'PUBLISHED', 2, NOW(), 1, 1, 0, 0),
    (2, '小区消防演练', '组织居民参与消防应急演练。', DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY) + INTERVAL 1 HOUR,
     NOW(), DATE_ADD(NOW(), INTERVAL 9 DAY), '幸福家园广场', 50, 'PUBLISHED', 4, NOW(), 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    activity_start_time = VALUES(activity_start_time),
    activity_end_time = VALUES(activity_end_time),
    signup_start_time = VALUES(signup_start_time),
    signup_end_time = VALUES(signup_end_time),
    location = VALUES(location),
    max_participants = VALUES(max_participants),
    status = VALUES(status),
    publisher_org_id = VALUES(publisher_org_id),
    publish_time = VALUES(publish_time),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

DELETE FROM biz_activity_scope WHERE activity_id IN (1, 2);
INSERT INTO biz_activity_scope (activity_id, scope_type, scope_ref_id)
VALUES
    (1, 'COMMUNITY', 2),
    (2, 'COMPLEX', 3);

-- ========== 字典初始化 ==========
INSERT INTO sys_dict_type (id, dict_code, dict_name, status, create_by, update_by, deleted, version)
VALUES
    (1, 'user_status', '用户状态', 1, 1, 1, 0, 0),
    (2, 'org_type', '组织类型', 1, 1, 1, 0, 0),
    (3, 'data_scope_type', '数据范围类型', 1, 1, 1, 0, 0),
    (4, 'notice_status', '公告状态', 1, 1, 1, 0, 0),
    (5, 'activity_status', '活动状态', 1, 1, 1, 0, 0),
    (6, 'repair_status', '报修状态', 1, 1, 1, 0, 0),
    (7, 'activity_signup_status', '活动报名状态', 1, 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    dict_name = VALUES(dict_name),
    status = VALUES(status),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

DELETE FROM sys_dict_data WHERE dict_type_code IN ('user_status', 'org_type', 'data_scope_type', 'notice_status', 'activity_status', 'repair_status', 'activity_signup_status');
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
('activity_status', '已发布', 'PUBLISHED', 2, 1, 1, 1, 0, 0),
('activity_status', '已撤回', 'RECALLED', 3, 1, 1, 1, 0, 0),
('activity_status', '已结束', 'FINISHED', 4, 1, 1, 1, 0, 0),
('activity_signup_status', '已报名', 'SIGNED', 1, 1, 1, 1, 0, 0),
('activity_signup_status', '已取消', 'CANCELED', 2, 1, 1, 1, 0, 0),
('repair_status', '待受理', 'PENDING_ACCEPT', 1, 1, 1, 1, 0, 0),
('repair_status', '已受理', 'ACCEPTED', 2, 1, 1, 1, 0, 0),
('repair_status', '已分派', 'ASSIGNED', 3, 1, 1, 1, 0, 0),
('repair_status', '处理中', 'PROCESSING', 4, 1, 1, 1, 0, 0),
('repair_status', '待确认', 'WAIT_CONFIRM', 5, 1, 1, 1, 0, 0),
('repair_status', '已完成', 'DONE', 6, 1, 1, 1, 0, 0),
('repair_status', '已关闭', 'CLOSED', 7, 1, 1, 1, 0, 0),
('repair_status', '已驳回', 'REJECTED', 8, 1, 1, 1, 0, 0),
('repair_status', '重新处理', 'REOPENED', 9, 1, 1, 1, 0, 0);


-- ========== 报修权限初始化 ==========
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, method, sort, create_by, update_by, deleted, version)
VALUES
    (1601, 'repair:create', '居民创建报修', 'API', NULL, '/api/repairs', 'POST', 71, 1, 1, 0, 0),
    (1602, 'repair:my:list', '我的报修列表', 'API', NULL, '/api/repairs/my', 'GET', 72, 1, 1, 0, 0),
    (1603, 'repair:my:view', '我的报修详情', 'API', NULL, '/api/repairs/my/{id}', 'GET', 73, 1, 1, 0, 0),
    (1604, 'repair:manage:list', '物业工单列表', 'API', NULL, '/api/repairs/manage', 'GET', 74, 1, 1, 0, 0),
    (1605, 'repair:manage:view', '物业工单详情', 'API', NULL, '/api/repairs/{id}', 'GET', 75, 1, 1, 0, 0),
    (1606, 'repair:accept', '物业受理工单', 'API', NULL, '/api/repairs/{id}/accept', 'POST', 76, 1, 1, 0, 0),
    (1607, 'repair:reject', '物业驳回工单', 'API', NULL, '/api/repairs/{id}/reject', 'POST', 77, 1, 1, 0, 0),
    (1608, 'repair:assign', '物业分派工单', 'API', NULL, '/api/repairs/{id}/assign', 'POST', 78, 1, 1, 0, 0),
    (1609, 'repair:take', '维修员接单', 'API', NULL, '/api/repairs/{id}/take', 'POST', 79, 1, 1, 0, 0),
    (1610, 'repair:process', '维修员处理工单', 'API', NULL, '/api/repairs/{id}/process', 'POST', 80, 1, 1, 0, 0),
    (1611, 'repair:submit', '维修员提交结果', 'API', NULL, '/api/repairs/{id}/submit', 'POST', 81, 1, 1, 0, 0),
    (1612, 'repair:confirm', '居民确认解决', 'API', NULL, '/api/repairs/{id}/confirm', 'POST', 82, 1, 1, 0, 0),
    (1613, 'repair:reopen', '居民反馈未解决', 'API', NULL, '/api/repairs/{id}/reopen', 'POST', 83, 1, 1, 0, 0),
    (1614, 'repair:evaluate', '居民评价工单', 'API', NULL, '/api/repairs/{id}/evaluate', 'POST', 84, 1, 1, 0, 0),
    (1615, 'repair:log:view', '查看工单流转日志', 'API', NULL, '/api/repairs/{id}/logs', 'GET', 85, 1, 1, 0, 0),
    (1616, 'repair:urge', '居民催单', 'API', NULL, '/api/repairs/{id}/urge', 'POST', 86, 1, 1, 0, 0),
    (1617, 'repair:close', '物业关闭工单', 'API', NULL, '/api/repairs/{id}/close', 'POST', 87, 1, 1, 0, 0)
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

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (2, 1601), (2, 1602), (2, 1603), (2, 1604), (2, 1605), (2, 1606), (2, 1607), (2, 1608), (2, 1609), (2, 1610),
    (2, 1611), (2, 1612), (2, 1613), (2, 1614), (2, 1615), (2, 1616), (2, 1617),
    (3, 1601), (3, 1602), (3, 1603), (3, 1604), (3, 1605), (3, 1606), (3, 1607), (3, 1608), (3, 1609), (3, 1610),
    (3, 1611), (3, 1612), (3, 1613), (3, 1614), (3, 1615), (3, 1616), (3, 1617),
    (4, 1601), (4, 1602), (4, 1603), (4, 1604), (4, 1605), (4, 1606), (4, 1607), (4, 1608), (4, 1609), (4, 1610),
    (4, 1611), (4, 1612), (4, 1613), (4, 1614), (4, 1615), (4, 1616), (4, 1617)
ON DUPLICATE KEY UPDATE permission_id = VALUES(permission_id);

-- ========== 报修示例数据 ==========
INSERT INTO biz_repair_order (
    id, order_no, title, description, contact_phone, repair_address, emergency_level, expect_handle_time,
    status, community_org_id, complex_org_id, property_company_org_id, resident_user_id,
    accept_user_id, assign_user_id, maintainer_user_id, process_desc, finish_desc, resident_feedback,
    evaluate_score, evaluate_content, urge_count, accepted_time, assigned_time, processing_time, finish_time,
    confirm_time, closed_time, last_urge_time, create_by, update_by, deleted, version
)
VALUES
    (1, 'RP202603240001', '厨房水管漏水', '厨房水槽下方持续漏水，影响正常生活。', '13800000000', '幸福家园小区1栋2单元301', 'HIGH', DATE_ADD(NOW(), INTERVAL 2 HOUR),
     'WAIT_CONFIRM', 2, 3, 4, 1,
     1, 1, 1, '已更换破损软管并测试。', '现场处理完成，观察30分钟未再漏水。', NULL,
     NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR),
     NULL, NULL, DATE_SUB(NOW(), INTERVAL 8 HOUR), 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    contact_phone = VALUES(contact_phone),
    repair_address = VALUES(repair_address),
    emergency_level = VALUES(emergency_level),
    expect_handle_time = VALUES(expect_handle_time),
    status = VALUES(status),
    community_org_id = VALUES(community_org_id),
    complex_org_id = VALUES(complex_org_id),
    property_company_org_id = VALUES(property_company_org_id),
    resident_user_id = VALUES(resident_user_id),
    accept_user_id = VALUES(accept_user_id),
    assign_user_id = VALUES(assign_user_id),
    maintainer_user_id = VALUES(maintainer_user_id),
    process_desc = VALUES(process_desc),
    finish_desc = VALUES(finish_desc),
    urge_count = VALUES(urge_count),
    accepted_time = VALUES(accepted_time),
    assigned_time = VALUES(assigned_time),
    processing_time = VALUES(processing_time),
    finish_time = VALUES(finish_time),
    last_urge_time = VALUES(last_urge_time),
    deleted = 0,
    update_by = 1,
    update_time = NOW();

DELETE FROM biz_repair_order_log WHERE repair_order_id = 1;
INSERT INTO biz_repair_order_log (repair_order_id, from_status, to_status, operation_type, operator_user_id, operation_remark, operation_time)
VALUES
    (1, NULL, 'PENDING_ACCEPT', 'CREATE', 1, '居民发起报修', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
    (1, 'PENDING_ACCEPT', 'ACCEPTED', 'ACCEPT', 1, '物业受理工单', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
    (1, 'ACCEPTED', 'ASSIGNED', 'ASSIGN', 1, '分派维修员', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
    (1, 'ASSIGNED', 'PROCESSING', 'TAKE', 1, '维修员接单', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
    (1, 'PROCESSING', 'WAIT_CONFIRM', 'SUBMIT', 1, '提交处理结果，等待居民确认', DATE_SUB(NOW(), INTERVAL 3 HOUR));

-- ========== 日志查询权限 ==========
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, method, sort, create_by, update_by, deleted, version)
VALUES
    (1701, 'log:login:list', '登录日志查询', 'API', NULL, '/api/logs/logins', 'GET', 91, 1, 1, 0, 0),
    (1702, 'log:operation:list', '操作日志查询', 'API', NULL, '/api/logs/operations', 'GET', 92, 1, 1, 0, 0)
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

INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (2, 1701), (2, 1702),
    (3, 1701), (3, 1702),
    (4, 1701), (4, 1702)
ON DUPLICATE KEY UPDATE permission_id = VALUES(permission_id);

-- ========== 超级管理员权限兜底同步 ==========
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE deleted = 0
ON DUPLICATE KEY UPDATE permission_id = VALUES(permission_id);

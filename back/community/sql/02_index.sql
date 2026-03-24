-- 社区服务系统数据库初始化脚本（索引）
SET NAMES utf8mb4;

-- sys_user
CREATE UNIQUE INDEX uk_sys_user_username ON sys_user (username);
CREATE INDEX idx_sys_user_phone ON sys_user (phone);
CREATE INDEX idx_sys_user_org_id ON sys_user (org_id);
CREATE INDEX idx_sys_user_status_deleted ON sys_user (status, deleted);
CREATE INDEX idx_sys_user_create_time ON sys_user (create_time);

-- sys_role
CREATE UNIQUE INDEX uk_sys_role_role_code ON sys_role (role_code);
CREATE INDEX idx_sys_role_status_deleted ON sys_role (status, deleted);
CREATE INDEX idx_sys_role_parent_role_id ON sys_role (parent_role_id);

-- sys_permission
CREATE UNIQUE INDEX uk_sys_permission_permission_code ON sys_permission (permission_code);
CREATE INDEX idx_sys_permission_parent_id ON sys_permission (parent_id);
CREATE INDEX idx_sys_permission_type_deleted ON sys_permission (permission_type, deleted);

-- sys_user_role
CREATE UNIQUE INDEX uk_sys_user_role_user_role ON sys_user_role (user_id, role_id);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role (role_id);

-- sys_role_permission
CREATE UNIQUE INDEX uk_sys_role_permission_role_perm ON sys_role_permission (role_id, permission_id);
CREATE INDEX idx_sys_role_permission_perm_id ON sys_role_permission (permission_id);

-- sys_role_scope
CREATE INDEX idx_sys_role_scope_role_id ON sys_role_scope (role_id);
CREATE INDEX idx_sys_role_scope_type_ref ON sys_role_scope (scope_type, scope_ref_id);

-- sys_org
CREATE UNIQUE INDEX uk_sys_org_org_code ON sys_org (org_code);
CREATE INDEX idx_sys_org_parent_id ON sys_org (parent_id);
CREATE INDEX idx_sys_org_org_type ON sys_org (org_type);
CREATE INDEX idx_sys_org_status_deleted ON sys_org (status, deleted);
CREATE INDEX idx_sys_org_ancestor_path ON sys_org (ancestor_path(191));

-- sys_dict_type & sys_dict_data
CREATE UNIQUE INDEX uk_sys_dict_type_dict_code ON sys_dict_type (dict_code);
CREATE INDEX idx_sys_dict_type_status_deleted ON sys_dict_type (status, deleted);
CREATE INDEX idx_sys_dict_data_type_code ON sys_dict_data (dict_type_code);
CREATE INDEX idx_sys_dict_data_status_deleted ON sys_dict_data (status, deleted);

-- biz_complex_property_rel
CREATE INDEX idx_biz_complex_property_rel_complex ON biz_complex_property_rel (complex_org_id);
CREATE INDEX idx_biz_complex_property_rel_property ON biz_complex_property_rel (property_company_org_id);
CREATE INDEX idx_biz_complex_property_rel_status_deleted ON biz_complex_property_rel (status, deleted);

-- biz_resident_profile
CREATE UNIQUE INDEX uk_biz_resident_profile_user_id ON biz_resident_profile (user_id);
CREATE INDEX idx_biz_resident_profile_community_complex ON biz_resident_profile (community_org_id, complex_org_id);
CREATE INDEX idx_biz_resident_profile_deleted ON biz_resident_profile (deleted);

-- biz_file_info
CREATE INDEX idx_biz_file_info_biz_type_biz_id ON biz_file_info (biz_type, biz_id);
CREATE INDEX idx_biz_file_info_uploader_id ON biz_file_info (uploader_id);
CREATE INDEX idx_biz_file_info_deleted ON biz_file_info (deleted);

-- log_login
CREATE INDEX idx_log_login_user_id ON log_login (user_id);
CREATE INDEX idx_log_login_success_time ON log_login (success_flag, login_time);
CREATE INDEX idx_log_login_username_time ON log_login (username, login_time);

-- log_operation
CREATE INDEX idx_log_operation_user_id ON log_operation (user_id);
CREATE INDEX idx_log_operation_module_time ON log_operation (operation_module, operation_time);
CREATE INDEX idx_log_operation_trace_id ON log_operation (trace_id);

-- biz_notice
CREATE INDEX idx_biz_notice_type_status_deleted ON biz_notice (notice_type, status, deleted);
CREATE INDEX idx_biz_notice_publisher_org_id ON biz_notice (publisher_org_id);
CREATE INDEX idx_biz_notice_publish_time ON biz_notice (publish_time);
CREATE INDEX idx_biz_notice_top_flag_publish_time ON biz_notice (top_flag, publish_time);

-- biz_notice_scope
CREATE INDEX idx_biz_notice_scope_notice_id ON biz_notice_scope (notice_id);
CREATE INDEX idx_biz_notice_scope_ref ON biz_notice_scope (scope_type, scope_ref_id);

-- biz_activity
CREATE INDEX idx_biz_activity_status_deleted ON biz_activity (status, deleted);
CREATE INDEX idx_biz_activity_publisher_org_id ON biz_activity (publisher_org_id);
CREATE INDEX idx_biz_activity_time ON biz_activity (activity_start_time, activity_end_time);
CREATE INDEX idx_biz_activity_signup_time ON biz_activity (signup_start_time, signup_end_time);
CREATE INDEX idx_biz_activity_publish_time ON biz_activity (publish_time);

-- biz_activity_scope
CREATE INDEX idx_biz_activity_scope_activity_id ON biz_activity_scope (activity_id);
CREATE INDEX idx_biz_activity_scope_ref ON biz_activity_scope (scope_type, scope_ref_id);

-- biz_activity_signup
CREATE UNIQUE INDEX uk_biz_activity_signup_activity_user ON biz_activity_signup (activity_id, user_id);
CREATE INDEX idx_biz_activity_signup_activity_status ON biz_activity_signup (activity_id, signup_status);
CREATE INDEX idx_biz_activity_signup_user_status ON biz_activity_signup (user_id, signup_status);

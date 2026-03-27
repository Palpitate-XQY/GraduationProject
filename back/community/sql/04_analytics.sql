-- Analytics module schema and permissions
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS biz_analytics_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    report_type VARCHAR(16) NOT NULL COMMENT 'DAILY/WEEKLY',
    report_date DATE NOT NULL COMMENT 'Anchor date of report',
    scope_key VARCHAR(128) NOT NULL COMMENT 'Scope cache key',
    scope_kind VARCHAR(32) NOT NULL COMMENT 'ALL/STREET/COMMUNITY/COMPLEX/PROPERTY_COMPANY/SELF/CUSTOM',
    period_start DATETIME NOT NULL COMMENT 'Period start datetime',
    period_end DATETIME NOT NULL COMMENT 'Period end datetime',
    metrics_json LONGTEXT NOT NULL COMMENT 'Metrics JSON',
    wordcloud_json LONGTEXT NOT NULL COMMENT 'Word cloud JSON',
    summary_markdown LONGTEXT NOT NULL COMMENT 'Summary markdown content',
    ai_mode VARCHAR(16) NOT NULL COMMENT 'AI/FALLBACK',
    model_name VARCHAR(128) DEFAULT NULL COMMENT 'AI model name',
    status VARCHAR(16) NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAILED',
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Generation time',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    UNIQUE KEY uk_biz_analytics_report_type_date_scope (report_type, report_date, scope_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Analytics report snapshot table';

CREATE INDEX idx_biz_analytics_report_scope_kind_date
    ON biz_analytics_report (scope_kind, report_date);
CREATE INDEX idx_biz_analytics_report_generated_at
    ON biz_analytics_report (generated_at);

INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, path, method, sort, create_by, update_by, deleted, version)
VALUES
    ('analytics:wordcloud:view', '查看词云分析', 'API', NULL, '/api/system/analytics/wordcloud', 'GET', 151, 1, 1, 0, 0),
    ('analytics:report:view', '查看分析报告', 'API', NULL, '/api/system/analytics/reports', 'GET', 152, 1, 1, 0, 0),
    ('analytics:report:generate', '手动生成分析报告', 'API', NULL, '/api/system/analytics/reports/generate', 'POST', 153, 1, 1, 0, 0)
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
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p
  ON p.permission_code IN ('analytics:wordcloud:view', 'analytics:report:view')
WHERE r.deleted = 0
  AND r.role_code IN ('SUPER_ADMIN', 'STREET_ADMIN', 'COMMUNITY_ADMIN', 'PROPERTY_ADMIN', 'MAINTAINER', 'REFIXX_MAN', 'RESIDENT')
ON DUPLICATE KEY UPDATE permission_id = VALUES(permission_id);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p
  ON p.permission_code IN ('analytics:report:generate')
WHERE r.deleted = 0
  AND r.role_code IN ('SUPER_ADMIN', 'STREET_ADMIN', 'COMMUNITY_ADMIN', 'PROPERTY_ADMIN')
ON DUPLICATE KEY UPDATE permission_id = VALUES(permission_id);

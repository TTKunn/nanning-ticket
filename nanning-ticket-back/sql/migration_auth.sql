-- ============================================================
--  AI 南宁票务管理系统 - 登录 / 账号模块迁移脚本
--  本脚本仅创建 sys_user 表结构。
--  5 个内置账号由后端 AuthBootstrap 在应用启动时自动写入（BCrypt 哈希）。
--  MySQL 8.0+ / utf8mb4
-- ============================================================

USE `nanning_ticket`;

SET NAMES utf8mb4;

-- ------------------------------------------------
--  系统用户表
--  - 管理员 / 运营 / 售票员 / 检票员 / 财务 等后台账号
--  - 密码使用 BCrypt 哈希存储（不可逆）
--  - 软删除通过 deleted_at 字段
--  - role 是主角色；roleCodes 存储多角色（逗号分隔）
-- ------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT            COMMENT '主键',
  `username`      VARCHAR(32)   NOT NULL                           COMMENT '登录账号（业务主键，唯一）',
  `password_hash` VARCHAR(128)  NOT NULL                           COMMENT 'BCrypt 密码哈希',
  `real_name`     VARCHAR(32)   NOT NULL                           COMMENT '真实姓名',
  `phone`         VARCHAR(20)   DEFAULT NULL                       COMMENT '手机号',
  `email`         VARCHAR(64)   DEFAULT NULL                       COMMENT '邮箱',
  `avatar`        VARCHAR(255)  DEFAULT NULL                       COMMENT '头像 URL（演示用，可空）',
  `role`          VARCHAR(32)   NOT NULL DEFAULT 'STAFF'           COMMENT '主角色：SUPER_ADMIN/ADMIN/OPERATOR/SELLER/VERIFIER/FINANCE/STAFF',
  `role_codes`    VARCHAR(255)  DEFAULT NULL                       COMMENT '角色编码集合（逗号分隔，含主角色）',
  `scenic_ids`    VARCHAR(255)  DEFAULT NULL                       COMMENT '可管辖园区 ID（逗号分隔），null/空 表示全部园区',
  `status`        VARCHAR(16)   NOT NULL DEFAULT '启用'            COMMENT '状态：启用/停用',
  `last_login_at` DATETIME      DEFAULT NULL                       COMMENT '最近登录时间',
  `last_login_ip` VARCHAR(64)   DEFAULT NULL                       COMMENT '最近登录 IP',
  `login_fail_count` INT         NOT NULL DEFAULT 0                 COMMENT '连续登录失败次数（达到上限后锁定）',
  `locked_until`  DATETIME      DEFAULT NULL                       COMMENT '账号锁定截止时间（NULL 表示未锁定）',
  `remark`        VARCHAR(255)  DEFAULT NULL                       COMMENT '备注',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`    DATETIME      DEFAULT NULL                       COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_phone`    (`phone`),
  KEY `idx_sys_user_email`    (`email`),
  KEY `idx_sys_user_role`     (`role`),
  KEY `idx_sys_user_status`   (`status`),
  KEY `idx_sys_user_deleted`  (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台系统用户表';

-- ------------------------------------------------
--  说明
--  内置账号（由 AuthBootstrap 启动时写入）：
--    admin   / admin123    - 超级管理员
--    manager / manager123  - 管理员
--    seller  / seller123   - 售票员（仅 1,2 号园区）
--    verify  / verify123   - 检票员（1,2,3 号园区）
--    finance / finance123  - 财务
--
--  如果你想手工插入账号，可使用以下方式：
--    1. 启动一次应用让 AuthBootstrap 生成 5 个内置账号；
--    2. 后台修改自己的密码（推荐）；
--    3. 或者使用任意 BCrypt 工具生成哈希后通过 SQL 更新：
--       UPDATE sys_user SET password_hash = '<bcrypt-hash>' WHERE username = 'admin';
--
--  一个在线 BCrypt 工具（仅供本地测试用，生产请自行用 at.favre.lib:bcrypt 离线生成）：
--    https://bcrypt-generator.com/
--  生成参数：rounds=10
-- ============================================================

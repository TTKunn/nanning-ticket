-- ============================================================
--  AI 南宁票务管理系统 - 数据库初始化脚本
--  本脚本包含已开发模块（园区、规则、票种、库存、销售、检票、票据、订单、渠道、系统设置）的表结构与种子数据
--  MySQL 8.0+ / utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `nanning_ticket`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `nanning_ticket`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------
--  园区表
-- ------------------------------------------------
DROP TABLE IF EXISTS `scenic`;
CREATE TABLE `scenic` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT            COMMENT '主键',
  `name`          VARCHAR(64)   NOT NULL                           COMMENT '园区名称',
  `icon`          VARCHAR(16)   DEFAULT NULL                       COMMENT '园区图标（单字或 emoji）',
  `icon_bg`       VARCHAR(16)   DEFAULT NULL                       COMMENT '园区图标背景色 HEX',
  `level`         VARCHAR(32)   DEFAULT NULL                       COMMENT '景区等级（国家5A级/城市公园等）',
  `address`       VARCHAR(255)  DEFAULT NULL                       COMMENT '详细地址',
  `open_time`     VARCHAR(32)   DEFAULT NULL                       COMMENT '开放时间（例：08:00-18:00）',
  `description`   TEXT          DEFAULT NULL                       COMMENT '园区说明',
  `project_count` INT           NOT NULL DEFAULT 0                 COMMENT '收费项目数（冗余字段）',
  `rule_count`    INT           NOT NULL DEFAULT 0                 COMMENT '规则数（冗余字段）',
  `ticket_count`  INT           NOT NULL DEFAULT 0                 COMMENT '票种数（冗余字段）',
  `month_sales`   DECIMAL(12,2) NOT NULL DEFAULT 0.00              COMMENT '本月销售额（冗余字段）',
  `status`        VARCHAR(16)   NOT NULL DEFAULT '运营中'          COMMENT '状态：运营中/暂停运营',
  `sort`          INT           NOT NULL DEFAULT 0                 COMMENT '排序值，越大越靠前',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`    DATETIME      DEFAULT NULL                       COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scenic_name` (`name`)                              COMMENT '园区名唯一',
  KEY `idx_scenic_status`  (`status`),
  KEY `idx_scenic_deleted` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='园区表';

-- ------------------------------------------------
--  项目规则表
-- ------------------------------------------------
DROP TABLE IF EXISTS `rule`;
CREATE TABLE `rule` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT                 COMMENT '主键',
  `scenic_id`       BIGINT        NOT NULL                                COMMENT '所属园区 ID',
  `name`            VARCHAR(64)   NOT NULL                                COMMENT '规则名称',
  `code`            VARCHAR(32)   NOT NULL                                COMMENT '规则编码（同一园区下唯一）',
  `type`            VARCHAR(16)   NOT NULL                                COMMENT '类型：折扣/免票/团体/时段/限流',
  `description`     VARCHAR(255)  DEFAULT NULL                            COMMENT '规则说明',
  `config`          TEXT          DEFAULT NULL                            COMMENT '规则参数 JSON',
  `priority`        INT           NOT NULL DEFAULT 0                      COMMENT '优先级，数值越大越优先',
  `status`          VARCHAR(16)   NOT NULL DEFAULT '启用'                 COMMENT '状态：启用/禁用',
  `effective_from`  DATE          DEFAULT NULL                            COMMENT '生效开始日期',
  `effective_to`    DATE          DEFAULT NULL                            COMMENT '生效结束日期',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`      DATETIME      DEFAULT NULL                            COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code_scenic` (`code`, `scenic_id`)                  COMMENT '同园区下规则编码唯一',
  KEY `idx_rule_scenic`  (`scenic_id`),
  KEY `idx_rule_type`    (`type`),
  KEY `idx_rule_status`  (`status`),
  KEY `idx_rule_deleted` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目规则表';

-- ------------------------------------------------
--  票种表
-- ------------------------------------------------
DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `scenic_id`     BIGINT        NOT NULL                               COMMENT '所属园区 ID',
  `name`          VARCHAR(64)   NOT NULL                               COMMENT '票种名称',
  `code`          VARCHAR(32)   NOT NULL                               COMMENT '票种编码（同一园区下唯一）',
  `category`      VARCHAR(16)   NOT NULL                               COMMENT '分类：单票/套票/联票',
  `price`         DECIMAL(10,2) NOT NULL                               COMMENT '票面价（销售价）',
  `cost_price`    DECIMAL(10,2) DEFAULT NULL                           COMMENT '成本价（用于对账）',
  `description`   VARCHAR(255)  DEFAULT NULL                           COMMENT '票种说明',
  `cover`         VARCHAR(255)  DEFAULT NULL                           COMMENT '封面图 URL',
  `tags`          VARCHAR(255)  DEFAULT NULL                           COMMENT '标签（逗号分隔：热销/推荐/限时）',
  `valid_days`    INT           NOT NULL DEFAULT 1                     COMMENT '入场有效天数（购票后 N 天内有效）',
  `refundable`    TINYINT(1)    NOT NULL DEFAULT 1                     COMMENT '是否可退：1=可退 0=不可退',
  `rule_ids`      VARCHAR(255)  DEFAULT NULL                           COMMENT '关联规则 ID（逗号分隔）',
  `status`        VARCHAR(16)   NOT NULL DEFAULT '在售'                COMMENT '状态：在售/停售',
  `sort`          INT           NOT NULL DEFAULT 0                     COMMENT '排序值，越大越靠前',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`    DATETIME      DEFAULT NULL                           COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_code_scenic` (`code`, `scenic_id`)             COMMENT '同园区下票种编码唯一',
  KEY `idx_ticket_scenic`   (`scenic_id`),
  KEY `idx_ticket_category` (`category`),
  KEY `idx_ticket_status`   (`status`),
  KEY `idx_ticket_deleted`  (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票种表';

-- ------------------------------------------------
--  库存表
-- ------------------------------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `ticket_id`       BIGINT        NOT NULL                               COMMENT '票种 ID',
  `scenic_id`       BIGINT        NOT NULL                               COMMENT '所属园区 ID（冗余，便于按园区过滤）',
  `inventory_date`  DATE          NOT NULL                               COMMENT '库存日期（票的入场日期）',
  `total`           INT           NOT NULL DEFAULT 0                     COMMENT '总库存',
  `sold`            INT           NOT NULL DEFAULT 0                     COMMENT '已售数量',
  `reserved`        INT           NOT NULL DEFAULT 0                     COMMENT '预占数量（未付款）',
  `available`       INT           NOT NULL DEFAULT 0                     COMMENT '可用库存（= total - sold - reserved）',
  `status`          VARCHAR(16)   NOT NULL DEFAULT '开放'                COMMENT '状态：开放/关闭/售罄',
  `remark`          VARCHAR(255)  DEFAULT NULL                           COMMENT '备注',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`      DATETIME      DEFAULT NULL                           COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_ticket_date` (`ticket_id`, `inventory_date`)  COMMENT '同票种同日期唯一',
  KEY `idx_inventory_scenic`  (`scenic_id`),
  KEY `idx_inventory_date`    (`inventory_date`),
  KEY `idx_inventory_status`  (`status`),
  KEY `idx_inventory_deleted` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- ------------------------------------------------
--  销售主单表（窗口售票）
--  说明：一条 sale 对应一次窗口出票（可包含多个票种明细）
-- ------------------------------------------------
DROP TABLE IF EXISTS `sale`;
CREATE TABLE `sale` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `sale_no`           VARCHAR(32)   NOT NULL                               COMMENT '销售流水号 e.g. S202606140001',
  `scenic_id`         BIGINT        NOT NULL                               COMMENT '园区 ID（冗余自首个明细的票种所属园区）',
  `window_id`         BIGINT        DEFAULT NULL                           COMMENT '窗口 ID（占位，对接系统/部门模块后填）',
  `window_name`       VARCHAR(64)   DEFAULT NULL                           COMMENT '窗口名称（冗余自窗口表）',
  `salesperson_id`    BIGINT        DEFAULT NULL                           COMMENT '售票员 ID（占位）',
  `salesperson_name`  VARCHAR(64)   DEFAULT NULL                           COMMENT '售票员姓名（冗余）',
  `visitor_name`      VARCHAR(64)   DEFAULT NULL                           COMMENT '取票/购票人姓名',
  `visitor_phone`     VARCHAR(20)   DEFAULT NULL                           COMMENT '联系电话',
  `visitor_id_card`   VARCHAR(32)   DEFAULT NULL                           COMMENT '身份证号',
  `sale_type`         VARCHAR(16)   NOT NULL DEFAULT '售票'                COMMENT '业务类型：售票/退票（占位字段；本表为正单）',
  `payment_method`    VARCHAR(16)   NOT NULL                               COMMENT '支付方式：现金/微信/支付宝/银行卡/余额',
  `total_amount`      DECIMAL(12,2) NOT NULL DEFAULT 0.00                  COMMENT '原价合计',
  `discount_amount`   DECIMAL(12,2) NOT NULL DEFAULT 0.00                  COMMENT '优惠合计',
  `paid_amount`       DECIMAL(12,2) NOT NULL DEFAULT 0.00                  COMMENT '实付金额（= total - discount）',
  `refund_amount`     DECIMAL(12,2) NOT NULL DEFAULT 0.00                  COMMENT '已退金额（部分退票时累加）',
  `item_count`        INT           NOT NULL DEFAULT 0                     COMMENT '票数合计（所有明细 quantity 之和）',
  `status`            VARCHAR(16)   NOT NULL DEFAULT '已支付'              COMMENT '状态：已支付/部分退票/已退票/已取消',
  `sale_time`         DATETIME      NOT NULL                               COMMENT '交易时间（售出时间）',
  `remark`            VARCHAR(255)  DEFAULT NULL                           COMMENT '备注',
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`        DATETIME      DEFAULT NULL                           COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sale_no` (`sale_no`)                                       COMMENT '销售流水号唯一',
  KEY `idx_sale_scenic`  (`scenic_id`),
  KEY `idx_sale_time`    (`sale_time`),
  KEY `idx_sale_status`  (`status`),
  KEY `idx_sale_phone`   (`visitor_phone`),
  KEY `idx_sale_deleted` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='窗口销售主单';

-- ------------------------------------------------
--  销售明细表
--  说明：一条 sale 可包含多个明细，每个明细对应一种票 × 一个入场日期
-- ------------------------------------------------
DROP TABLE IF EXISTS `sale_item`;
CREATE TABLE `sale_item` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `sale_id`           BIGINT        NOT NULL                               COMMENT '销售主单 ID',
  `ticket_id`         BIGINT        NOT NULL                               COMMENT '票种 ID',
  `ticket_name`       VARCHAR(128)  DEFAULT NULL                           COMMENT '票种名称（冗余）',
  `scenic_id`         BIGINT        DEFAULT NULL                           COMMENT '园区 ID（冗余）',
  `inventory_id`      BIGINT        DEFAULT NULL                           COMMENT '对应库存记录 ID',
  `inventory_date`    DATE          DEFAULT NULL                           COMMENT '入场日期',
  `unit_price`        DECIMAL(10,2) NOT NULL DEFAULT 0.00                  COMMENT '销售单价',
  `quantity`          INT           NOT NULL DEFAULT 0                     COMMENT '购买数量',
  `subtotal_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00                  COMMENT '原价小计 = unit_price * quantity',
  `rule_ids`          VARCHAR(255)  DEFAULT NULL                           COMMENT '应用规则 ID（逗号分隔）',
  `discount_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00                  COMMENT '优惠金额',
  `final_amount`      DECIMAL(10,2) NOT NULL DEFAULT 0.00                  COMMENT '实付小计 = subtotal - discount',
  `voucher_codes`     VARCHAR(1024) DEFAULT NULL                           COMMENT '票据码（逗号分隔，每张票一个码，预留给票据模块）',
  `refund_quantity`   INT           NOT NULL DEFAULT 0                     COMMENT '已退数量（≤ quantity）',
  `refund_amount`     DECIMAL(10,2) NOT NULL DEFAULT 0.00                  COMMENT '已退金额',
  `remark`            VARCHAR(255)  DEFAULT NULL                           COMMENT '备注',
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`        DATETIME      DEFAULT NULL                           COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_sale`      (`sale_id`),
  KEY `idx_item_ticket`    (`ticket_id`),
  KEY `idx_item_inventory` (`inventory_id`),
  KEY `idx_item_date`      (`inventory_date`),
  KEY `idx_item_deleted`   (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='窗口销售明细';

-- ------------------------------------------------
--  检票记录表
--  说明：每次检票尝试都生成一条记录（无论成功/失败），用于审计与统计
--  一个票据码同时仅允许一条 result=成功 的记录（由 Service 层 check-then-insert 保证）
-- ------------------------------------------------
DROP TABLE IF EXISTS `verify_record`;
CREATE TABLE `verify_record` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `voucher_code`      VARCHAR(64)   NOT NULL                               COMMENT '被检票据码',
  `sale_id`           BIGINT        DEFAULT NULL                           COMMENT '所属销售单 ID',
  `sale_item_id`      BIGINT        DEFAULT NULL                           COMMENT '所属销售明细 ID',
  `ticket_id`         BIGINT        DEFAULT NULL                           COMMENT '票种 ID',
  `ticket_name`       VARCHAR(128)  DEFAULT NULL                           COMMENT '票种名称（冗余）',
  `scenic_id`         BIGINT        DEFAULT NULL                           COMMENT '园区 ID（冗余）',
  `inventory_id`      BIGINT        DEFAULT NULL                           COMMENT '对应库存记录 ID',
  `inventory_date`    DATE          DEFAULT NULL                           COMMENT '入场日期',
  `verify_time`       DATETIME      NOT NULL                               COMMENT '检票时间',
  `verify_method`     VARCHAR(16)   NOT NULL DEFAULT '扫码'                COMMENT '检票方式：扫码/手输/刷脸',
  `verify_staff_id`   BIGINT        DEFAULT NULL                           COMMENT '检票员 ID（占位）',
  `verify_staff_name` VARCHAR(64)   DEFAULT NULL                           COMMENT '检票员姓名（冗余）',
  `device_id`         BIGINT        DEFAULT NULL                           COMMENT '闸机/设备 ID（占位）',
  `device_name`       VARCHAR(64)   DEFAULT NULL                           COMMENT '设备名称（冗余）',
  `result`            VARCHAR(16)   NOT NULL DEFAULT '成功'                COMMENT '检票结果：成功/失败',
  `fail_reason`       VARCHAR(255)  DEFAULT NULL                           COMMENT '失败原因：已使用/已过期/未生效/销售单已退/无效码',
  `visitor_name`      VARCHAR(64)   DEFAULT NULL                           COMMENT '购票人（冗余）',
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`        DATETIME      DEFAULT NULL                           COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_verify_code`     (`voucher_code`),
  KEY `idx_verify_sale`     (`sale_id`),
  KEY `idx_verify_saleitem` (`sale_item_id`),
  KEY `idx_verify_scenic`   (`scenic_id`),
  KEY `idx_verify_time`     (`verify_time`),
  KEY `idx_verify_result`   (`result`),
  KEY `idx_verify_deleted`  (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检票记录';

DROP TABLE IF EXISTS `voucher`;
CREATE TABLE `voucher` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `voucher_code`     VARCHAR(64)   NOT NULL                               COMMENT '票据码（唯一）',
  `qr_code`          VARCHAR(255)  DEFAULT NULL                           COMMENT '二维码内容/URL（冗余）',
  `status`           VARCHAR(16)   NOT NULL DEFAULT '待使用'              COMMENT '状态：待使用/已使用/已退/已作废',
  `source_type`      VARCHAR(16)   NOT NULL DEFAULT 'SALE'                COMMENT '票据来源：SALE=窗口售票，ORDER=在线订单',
  `sale_id`          BIGINT        DEFAULT NULL                           COMMENT '所属销售单 ID（sourceType=SALE 时使用）',
  `sale_item_id`     BIGINT        DEFAULT NULL                           COMMENT '所属销售明细 ID（sourceType=SALE 时使用）',
  `order_id`         BIGINT        DEFAULT NULL                           COMMENT '所属订单 ID（sourceType=ORDER 时使用）',
  `order_item_id`    BIGINT        DEFAULT NULL                           COMMENT '所属订单明细 ID（sourceType=ORDER 时使用）',
  `ticket_id`        BIGINT        NOT NULL                               COMMENT '票种 ID',
  `ticket_name`      VARCHAR(128)  NOT NULL                               COMMENT '票种名称（冗余）',
  `scenic_id`        BIGINT        NOT NULL                               COMMENT '园区 ID（冗余）',
  `scenic_name`      VARCHAR(128)  DEFAULT NULL                           COMMENT '园区名称（冗余）',
  `inventory_id`     BIGINT        DEFAULT NULL                           COMMENT '对应库存记录 ID',
  `inventory_date`   DATE          DEFAULT NULL                           COMMENT '入场日期',
  `valid_from`       DATE          DEFAULT NULL                           COMMENT '入场有效期起',
  `valid_to`         DATE          DEFAULT NULL                           COMMENT '入场有效期止',
  `visitor_name`     VARCHAR(64)   DEFAULT NULL                           COMMENT '购票人姓名',
  `visitor_phone`    VARCHAR(32)   DEFAULT NULL                           COMMENT '购票人手机',
  `visitor_id_card`  VARCHAR(32)   DEFAULT NULL                           COMMENT '购票人身份证',
  `use_time`         DATETIME      DEFAULT NULL                           COMMENT '核销时间',
  `use_staff_id`     BIGINT        DEFAULT NULL                           COMMENT '检票员 ID',
  `use_staff_name`   VARCHAR(64)   DEFAULT NULL                           COMMENT '检票员姓名（冗余）',
  `device_id`        BIGINT        DEFAULT NULL                           COMMENT '设备 ID',
  `device_name`      VARCHAR(64)   DEFAULT NULL                           COMMENT '设备名称（冗余）',
  `print_count`      INT           NOT NULL DEFAULT 0                      COMMENT '打印次数',
  `last_print_time`  DATETIME      DEFAULT NULL                           COMMENT '最近打印时间',
  `issue_time`       DATETIME      NOT NULL                               COMMENT '出票时间（与创建时间解耦，便于回填历史）',
  `revoke_time`      DATETIME      DEFAULT NULL                           COMMENT '作废时间',
  `revoke_reason`    VARCHAR(255)  DEFAULT NULL                           COMMENT '作废原因',
  `revoke_staff`     VARCHAR(64)   DEFAULT NULL                           COMMENT '作废操作员',
  `remark`           VARCHAR(500)  DEFAULT NULL                           COMMENT '备注',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`       DATETIME      DEFAULT NULL                           COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_voucher_code`   (`voucher_code`),
  KEY `idx_voucher_sale`        (`sale_id`),
  KEY `idx_voucher_saleitem`    (`sale_item_id`),
  KEY `idx_voucher_source`      (`source_type`),
  KEY `idx_voucher_order`       (`order_id`),
  KEY `idx_voucher_orderitem`   (`order_item_id`),
  KEY `idx_voucher_ticket`      (`ticket_id`),
  KEY `idx_voucher_scenic`      (`scenic_id`),
  KEY `idx_voucher_status`      (`status`),
  KEY `idx_voucher_inventory`   (`inventory_id`),
  KEY `idx_voucher_date`        (`inventory_date`),
  KEY `idx_voucher_valid_to`    (`valid_to`),
  KEY `idx_voucher_deleted`     (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票据（每张票一个独立实体，支持单张作废/补发）';

DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `order_id`         BIGINT        NOT NULL                               COMMENT '所属订单 ID',
  `ticket_id`        BIGINT        NOT NULL                               COMMENT '票种 ID',
  `ticket_name`      VARCHAR(128)  NOT NULL                               COMMENT '票种名称（冗余）',
  `scenic_id`        BIGINT        NOT NULL                               COMMENT '园区 ID（冗余）',
  `inventory_id`     BIGINT        DEFAULT NULL                           COMMENT '对应库存记录 ID',
  `inventory_date`   DATE          NOT NULL                               COMMENT '入场日期',
  `unit_price`       DECIMAL(10,2) NOT NULL                               COMMENT '销售单价',
  `quantity`         INT           NOT NULL                               COMMENT '购买数量',
  `subtotal_amount`  DECIMAL(12,2) NOT NULL                               COMMENT '原价小计 = unitPrice * quantity',
  `rule_ids`         VARCHAR(255)  DEFAULT NULL                           COMMENT '应用规则 ID（逗号分隔）',
  `discount_amount`  DECIMAL(12,2) NOT NULL DEFAULT 0.00                 COMMENT '优惠金额',
  `final_amount`     DECIMAL(12,2) NOT NULL                               COMMENT '实付小计 = subtotal - discount',
  `voucher_codes`    VARCHAR(2000) DEFAULT NULL                           COMMENT '票据码（逗号分隔）',
  `refund_quantity`  INT           NOT NULL DEFAULT 0                      COMMENT '已退数量（≤ quantity）',
  `refund_amount`    DECIMAL(12,2) NOT NULL DEFAULT 0.00                 COMMENT '已退金额',
  `remark`           VARCHAR(500)  DEFAULT NULL                           COMMENT '备注',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`       DATETIME      DEFAULT NULL                           COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_orderitem_order`  (`order_id`),
  KEY `idx_orderitem_ticket` (`ticket_id`),
  KEY `idx_orderitem_scenic` (`scenic_id`),
  KEY `idx_orderitem_date`   (`inventory_date`),
  KEY `idx_orderitem_deleted`(`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线订单明细';

DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id`                  BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `order_no`            VARCHAR(64)   NOT NULL                               COMMENT '订单流水号（唯一）',
  `channel_code`        VARCHAR(32)   NOT NULL                               COMMENT '渠道编码：OTA/官网/小程序/APP/抖音/微信',
  `channel_name`        VARCHAR(64)   NOT NULL                               COMMENT '渠道名称（冗余）',
  `scenic_id`           BIGINT        NOT NULL                               COMMENT '园区 ID',
  `scenic_name`         VARCHAR(128)  DEFAULT NULL                           COMMENT '园区名称（冗余）',
  `user_id`             BIGINT        DEFAULT NULL                           COMMENT '下单用户 ID（占位，对接会员系统后填）',
  `user_name`           VARCHAR(64)   DEFAULT NULL                           COMMENT '下单用户名（冗余）',
  `contact_name`        VARCHAR(64)   NOT NULL                               COMMENT '联系人姓名',
  `contact_phone`       VARCHAR(32)   NOT NULL                               COMMENT '联系人手机',
  `contact_id_card`     VARCHAR(32)   DEFAULT NULL                           COMMENT '联系人身份证',
  `total_amount`        DECIMAL(12,2) NOT NULL                               COMMENT '订单原价总额',
  `discount_amount`     DECIMAL(12,2) NOT NULL DEFAULT 0.00                 COMMENT '优惠金额',
  `paid_amount`         DECIMAL(12,2) NOT NULL DEFAULT 0.00                 COMMENT '实付金额 = total - discount',
  `refund_amount`       DECIMAL(12,2) NOT NULL DEFAULT 0.00                 COMMENT '已退金额',
  `item_count`          INT           NOT NULL DEFAULT 0                      COMMENT '票数合计',
  `pay_method`          VARCHAR(16)   DEFAULT NULL                           COMMENT '支付方式：微信/支付宝/银行卡/余额',
  `pay_time`            DATETIME      DEFAULT NULL                           COMMENT '支付时间',
  `pay_transaction_id`  VARCHAR(64)   DEFAULT NULL                           COMMENT '支付平台流水号',
  `status`              VARCHAR(16)   NOT NULL DEFAULT '待支付'              COMMENT '订单状态：待支付/已出票/已取消/退款中/已退款/部分退款',
  `fulfill_time`        DATETIME      DEFAULT NULL                           COMMENT '出票时间（生成 voucher 的时间）',
  `cancel_time`         DATETIME      DEFAULT NULL                           COMMENT '取消时间',
  `cancel_reason`       VARCHAR(255)  DEFAULT NULL                           COMMENT '取消原因',
  `refund_time`         DATETIME      DEFAULT NULL                           COMMENT '退款完成时间',
  `order_time`          DATETIME      NOT NULL                               COMMENT '下单时间',
  `use_start_date`      DATE          DEFAULT NULL                           COMMENT '入场起始日期（最早一张票）',
  `use_end_date`        DATE          DEFAULT NULL                           COMMENT '入场结束日期（最晚一张票）',
  `remark`              VARCHAR(500)  DEFAULT NULL                           COMMENT '备注',
  `created_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  `updated_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`          DATETIME      DEFAULT NULL                           COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no`      (`order_no`),
  KEY `idx_order_channel`      (`channel_code`),
  KEY `idx_order_scenic`       (`scenic_id`),
  KEY `idx_order_user`         (`user_id`),
  KEY `idx_order_contact_phone`(`contact_phone`),
  KEY `idx_order_status`       (`status`),
  KEY `idx_order_pay_time`     (`pay_time`),
  KEY `idx_order_fulfill_time` (`fulfill_time`),
  KEY `idx_order_time`         (`order_time`),
  KEY `idx_order_use_range`    (`use_start_date`,`use_end_date`),
  KEY `idx_order_deleted`      (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线订单（区别于窗口销售 sale）';

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------
--  种子数据
-- ------------------------------------------------
INSERT INTO `scenic`
  (`name`, `icon`, `icon_bg`, `level`, `address`, `open_time`, `description`, `status`, `sort`, `project_count`, `rule_count`, `ticket_count`, `month_sales`)
VALUES
  ('青秀山风景区', '山', '#f0fdf4', '国家5A级景区', '南宁市青秀区青秀山路', '06:00-22:00', '青秀山是南宁的标志性景区，含观光车、索道、佛教文化等项目。', '运营中', 100, 4, 3, 5, 24720.00),
  ('南湖公园',     '湖', '#eff6ff', '城市公园',     '南宁市青秀区民族大道', '06:00-23:00', '环湖步道+夜跑热门地，节假日会推出主题活动套票。',         '运营中', 90,  2, 2, 2, 14400.00),
  ('邕江景区',     '江', '#fff7ed', '夜游景区',     '南宁市邕江沿岸',     '19:00-22:30', '夜游邕江项目，可选游船+演艺组合套餐。',                  '运营中', 80,  3, 2, 3, 13640.00);

-- ------------------------------------------------
--  规则种子数据
-- ------------------------------------------------
INSERT INTO `rule` (`scenic_id`, `name`, `code`, `type`, `description`, `config`, `priority`, `status`, `effective_from`, `effective_to`)
VALUES
  (1, '儿童票 8 折',     'CHILD_DISCOUNT',    '折扣', '身高 1.2-1.4 米儿童享 8 折优惠',     '{"rate":0.8,"minHeight":1.2,"maxHeight":1.4}', 100, '启用', '2026-01-01', '2026-12-31'),
  (1, '老人免票',         'SENIOR_FREE',       '免票', '65 周岁及以上老人凭身份证免票',       '{"minAge":65,"needIdCard":true}',              90,  '启用', '2026-01-01', NULL),
  (1, '团体 10 人 9 折', 'GROUP_DISCOUNT',    '团体', '同一园区同一天满 10 人享 9 折',       '{"minCount":10,"rate":0.9}',                    80,  '启用', '2026-01-01', NULL),
  (1, '节假日加价 20%',  'HOLIDAY_SURCHARGE', '时段', '春节、国庆等节假日加价 20%',         '{"rate":1.2,"scope":"holiday"}',                70,  '禁用', '2026-01-01', '2026-12-31'),
  (2, '夜跑免费入场',     'NIGHT_RUN_FREE',    '时段', '每日 20:00 后免费入场',               '{"startTime":"20:00","endTime":"22:00"}',      100, '启用', '2026-01-01', NULL),
  (2, '单日限流 3000 人','DAILY_QUOTA',       '限流', '单日预约总量上限 3000 人',            '{"maxCount":3000,"perDay":true}',               100, '启用', '2026-01-01', NULL),
  (3, '游船团体套餐',     'BOAT_GROUP',        '团体', '游船团体套餐 20 人起订',              '{"minCount":20,"rate":0.85}',                   90,  '启用', '2026-01-01', NULL);

-- ------------------------------------------------
--  票种种子数据
-- ------------------------------------------------
INSERT INTO `ticket`
  (`scenic_id`, `name`, `code`, `category`, `price`, `cost_price`, `description`, `cover`, `tags`, `valid_days`, `refundable`, `rule_ids`, `status`, `sort`)
VALUES
  (1, '青秀山成人票',        'QXS_ADULT',       '单票',  60.00,  40.00, '青秀山风景区成人票，入园当日有效',            NULL,                '热销,推荐', 1, 1, '1,3',         '在售', 100),
  (1, '青秀山学生票',        'QXS_STUDENT',     '单票',  30.00,  20.00, '全日制学生凭学生证购买',                       NULL,                '学生',      1, 1, '1,3',         '在售', 90),
  (1, '青秀山亲子套票',      'QXS_FAMILY',      '套票', 128.00,  90.00, '1 大 1 小套票，含观光车票',                     NULL,                '推荐,亲子',  1, 1, '1,3',         '在售', 95),
  (1, '青秀山+索道联票',     'QXS_CABLEWAY',    '联票',  98.00,  60.00, '青秀山门票+索道往返',                            NULL,                '联票,索道',  1, 1, '1,3',         '在售', 85),
  (1, '青秀山老人票',        'QXS_SENIOR',      '单票',   0.00,   0.00, '65 周岁及以上老人凭身份证免票',                 NULL,                '免票',      1, 0, '1,2',         '在售', 80),
  (2, '南湖公园免费票',      'NH_FREE',         '单票',   0.00,   0.00, '南湖公园免费入场',                              NULL,                '免费',      1, 0, NULL,          '在售', 100),
  (2, '南湖夜跑纪念票',      'NH_NIGHT_RUN',    '单票',  19.90,  10.00, '夜跑活动纪念票，含号码布',                      NULL,                '夜跑,纪念',  1, 0, '5',           '在售', 90),
  (3, '邕江夜游船票',        'YJ_BOAT',         '单票',  68.00,  40.00, '邕江夜游 90 分钟游船',                          NULL,                '夜游,游船',  1, 1, '1',           '在售', 100),
  (3, '邕江夜游+演艺套票',   'YJ_BOAT_SHOW',    '套票', 138.00,  90.00, '夜游船票+沿岸演艺观赏',                         NULL,                '套票,演艺',  1, 1, '1,7',         '在售', 95);

-- ------------------------------------------------
--  库存种子数据
--  说明：演示用近 7 天数据，每天给青秀山成人票、夜游船票各设 500 张库存
-- ------------------------------------------------
INSERT INTO `inventory`
  (`ticket_id`, `scenic_id`, `inventory_date`, `total`, `sold`, `reserved`, `available`, `status`, `remark`)
VALUES
  -- 票种 1：青秀山成人票，2026-06-14 ~ 2026-06-20，每天 500 张
  (1, 1, '2026-06-14', 500, 120,  10, 370, '开放', '工作日'),
  (1, 1, '2026-06-15', 500,  80,   5, 415, '开放', '工作日'),
  (1, 1, '2026-06-16', 500, 200,  20, 280, '开放', '工作日'),
  (1, 1, '2026-06-17', 500, 320,  15, 165, '开放', '工作日'),
  (1, 1, '2026-06-18', 500, 480,  20,   0, '售罄', '周末高峰'),
  (1, 1, '2026-06-19', 500, 460,  10,  30, '开放', '周末'),
  (1, 1, '2026-06-20', 500, 350,  30, 120, '开放', '周末'),
  -- 票种 3：青秀山亲子套票，单日小批量
  (3, 1, '2026-06-14', 100,  20,   2,  78, '开放', ''),
  (3, 1, '2026-06-15', 100,  15,   0,  85, '开放', ''),
  -- 票种 8：邕江夜游船票
  (8, 3, '2026-06-14', 200, 180,   5,  15, '开放', '夜游'),
  (8, 3, '2026-06-15', 200, 200,   0,   0, '售罄', '夜游'),
  (8, 3, '2026-06-16', 200, 150,  10,  40, '开放', '夜游');

-- ------------------------------------------------
--  销售种子数据（演示用历史流水 4 条）
-- ------------------------------------------------
INSERT INTO `sale`
  (`sale_no`, `scenic_id`, `window_name`, `salesperson_name`,
   `visitor_name`, `visitor_phone`, `payment_method`,
   `total_amount`, `discount_amount`, `paid_amount`, `refund_amount`,
   `item_count`, `status`, `sale_time`, `remark`)
VALUES
  ('S202606140001', 1, '1号窗口', '李华', '王芳',   '13800000001', '微信',   120.00,  0.00, 120.00,  0.00, 2, '已支付', '2026-06-14 09:15:22', '现场购票'),
  ('S202606140002', 1, '1号窗口', '李华', '陈东',   '13800000002', '支付宝',  60.00,  0.00,  60.00,  0.00, 1, '已支付', '2026-06-14 10:02:10', ''),
  ('S202606140003', 2, '2号窗口', '黄敏', '林雪',   '13800000003', '现金',    19.90,  0.00,  19.90, 19.90, 1, '已退票', '2026-06-14 19:30:00', '买错日期'),
  ('S202606150001', 3, '3号窗口', '黄敏', '赵明',   '13800000004', '银行卡', 136.00,  0.00, 136.00, 68.00, 2, '部分退票', '2026-06-15 20:10:45', '退其中 1 张');

INSERT INTO `sale_item`
  (`sale_id`, `ticket_id`, `ticket_name`, `scenic_id`, `inventory_id`, `inventory_date`,
   `unit_price`, `quantity`, `subtotal_amount`, `rule_ids`, `discount_amount`, `final_amount`,
   `voucher_codes`, `refund_quantity`, `refund_amount`)
VALUES
  -- 1 号单：2 张青秀山成人票
  (1, 1, '青秀山成人票',  1, 1,  '2026-06-14', 60.00, 2, 120.00, '1,3', 0.00, 120.00, 'V202606140001,V202606140002', 0, 0.00),
  -- 2 号单：1 张青秀山成人票
  (2, 1, '青秀山成人票',  1, 1,  '2026-06-14', 60.00, 1,  60.00, '1,3', 0.00,  60.00, 'V202606140003',                0, 0.00),
  -- 3 号单：1 张南湖夜跑纪念票（已全退）
  (3, 7, '南湖夜跑纪念票', 2, NULL, '2026-06-14', 19.90, 1,  19.90, '5',   0.00,  19.90, 'V202606140004',                1, 19.90),
  -- 4 号单：2 张邕江夜游船票（已退 1 张）
  (4, 8, '邕江夜游船票',  3, 10, '2026-06-15', 68.00, 2, 136.00, '1',   0.00, 136.00, 'V202606150001,V202606150002',  1, 68.00);

-- ------------------------------------------------
--  检票种子数据（演示用检票历史 3 条：2 成功 + 1 失败）
--  说明：V202606140001 / V202606140003 已核销；V00000000 触发"无效码"失败
-- ------------------------------------------------
INSERT INTO `verify_record`
  (`voucher_code`, `sale_id`, `sale_item_id`, `ticket_id`, `ticket_name`, `scenic_id`,
   `inventory_id`, `inventory_date`, `verify_time`, `verify_method`, `verify_staff_name`,
   `device_name`, `result`, `fail_reason`, `visitor_name`)
VALUES
  ('V202606140001', 1, 1, 1, '青秀山成人票', 1, 1, '2026-06-14', '2026-06-14 09:30:00',
   '扫码', '张检', '1号闸机', '成功', NULL,            '王芳'),
  ('V202606140003', 2, 2, 1, '青秀山成人票', 1, 1, '2026-06-14', '2026-06-14 10:10:30',
   '扫码', '张检', '1号闸机', '成功', NULL,            '陈东'),
  ('V00000000',     NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-14 11:00:00',
   '手输', '张检', '1号闸机', '失败', '无效码',         '匿名');

-- ------------------------------------------------
--  票据种子数据（对应 sales/sale_items 中已售出的票据码）
--  说明：
--    - V202606140001 / V202606140003 已在 verify_record 中核销 → 状态：已使用
--    - V202606140002 尚未核销 → 状态：待使用
--    - V202606140004 所属销售单已全退 → 状态：已退
--    - V202606150001 所属销售单已部分退（退的是 V202606150002）→ 待使用
--    - V202606150002 已退（被退的那张）→ 状态：已退
--    - V00000000 是无效码，对应 verify_record 失败记录，不在 voucher 表中
-- ------------------------------------------------
INSERT INTO `voucher`
  (`voucher_code`, `qr_code`, `status`,
   `sale_id`, `sale_item_id`, `ticket_id`, `ticket_name`,
   `scenic_id`, `scenic_name`, `inventory_id`, `inventory_date`,
   `valid_from`, `valid_to`,
   `visitor_name`, `visitor_phone`, `visitor_id_card`,
   `use_time`, `use_staff_name`, `device_name`,
   `print_count`, `last_print_time`, `issue_time`,
   `revoke_time`, `revoke_reason`, `remark`)
VALUES
  -- 1 号单：青秀山成人票 2 张
  ('V202606140001', 'V202606140001', '已使用', 1, 1, 1, '青秀山成人票', 1, '青秀山风景区',
   1, '2026-06-14', '2026-06-14', '2026-06-14', '王芳', '13800000001', NULL,
   '2026-06-14 09:30:00', '张检', '1号闸机', 0, NULL, '2026-06-14 09:15:22',
   NULL, NULL, '现场购票'),
  ('V202606140002', 'V202606140002', '待使用', 1, 1, 1, '青秀山成人票', 1, '青秀山风景区',
   1, '2026-06-14', '2026-06-14', '2026-06-14', '王芳', '13800000001', NULL,
   NULL, NULL, NULL, 0, NULL, '2026-06-14 09:15:22',
   NULL, NULL, '现场购票'),
  -- 2 号单：青秀山成人票 1 张
  ('V202606140003', 'V202606140003', '已使用', 2, 2, 1, '青秀山成人票', 1, '青秀山风景区',
   1, '2026-06-14', '2026-06-14', '2026-06-14', '陈东', '13800000002', NULL,
   '2026-06-14 10:10:30', '张检', '1号闸机', 0, NULL, '2026-06-14 10:02:10',
   NULL, NULL, ''),
  -- 3 号单：南湖夜跑纪念票 1 张（销售单全退）
  ('V202606140004', 'V202606140004', '已退',   3, 3, 7, '南湖夜跑纪念票', 2, '南湖公园',
   NULL, '2026-06-14', '2026-06-14', '2026-06-14', '林雪', '13800000003', NULL,
   NULL, NULL, NULL, 0, NULL, '2026-06-14 19:30:00',
   '2026-06-14 19:45:00', '客户买错日期', '买错日期'),
  -- 4 号单：邕江夜游船票 2 张（已退 1 张 = V202606150002）
  ('V202606150001', 'V202606150001', '待使用', 4, 4, 8, '邕江夜游船票', 3, '邕江夜游码头',
   10, '2026-06-15', '2026-06-15', '2026-06-15', '赵明', '13800000004', NULL,
   NULL, NULL, NULL, 0, NULL, '2026-06-15 20:10:45',
   NULL, NULL, '退其中 1 张'),
  ('V202606150002', 'V202606150002', '已退',   4, 4, 8, '邕江夜游船票', 3, '邕江夜游码头',
   10, '2026-06-15', '2026-06-15', '2026-06-15', '赵明', '13800000004', NULL,
   NULL, NULL, NULL, 0, NULL, '2026-06-15 20:10:45',
   '2026-06-15 20:30:00', '客户取消其中 1 张', '退其中 1 张');

-- ------------------------------------------------
--  在线订单种子数据（演示用：3 单覆盖 待支付/已出票/已退款 三状态）
--    - O202606140001：小程序下单、待支付（模拟未付款）
--    - O202606140002：官网下单、已出票、出 1 张青秀山成人票
--    - O202606140003：OTA 下单、已退款（演示退款链路，关联 voucher 也设"已退"）
-- ------------------------------------------------
INSERT INTO `order`
  (`order_no`, `channel_code`, `channel_name`, `scenic_id`, `scenic_name`,
   `user_name`, `contact_name`, `contact_phone`, `contact_id_card`,
   `total_amount`, `discount_amount`, `paid_amount`, `refund_amount`, `item_count`,
   `pay_method`, `pay_time`, `pay_transaction_id`,
   `status`, `fulfill_time`, `cancel_time`, `cancel_reason`,
   `refund_time`, `order_time`,
   `use_start_date`, `use_end_date`, `remark`)
VALUES
  -- 1 号单：青秀山 2 张成人票，待支付
  ('O202606140001', '小程序', '微信小程序', 1, '青秀山风景区',
   '小王', '小王', '13900000001', NULL,
   120.00, 0.00,   0.00,   0.00, 2,
   NULL, NULL, NULL,
   '待支付', NULL, NULL, NULL,
   NULL, '2026-06-14 08:30:00',
   '2026-06-20', '2026-06-20', '在线下单未付款'),
  -- 2 号单：青秀山 1 张亲子套票，已出票
  ('O202606140002', '官网', '官方商城', 1, '青秀山风景区',
   '小李', '小李', '13900000002', '450101199001011234',
   128.00, 0.00, 128.00,   0.00, 1,
   '微信', '2026-06-14 09:00:00', 'WX2026061409000123',
   '已出票', '2026-06-14 09:00:30', NULL, NULL,
   NULL, '2026-06-14 08:55:00',
   '2026-06-21', '2026-06-21', '1 大 1 小'),
  -- 3 号单：南湖夜跑纪念票 1 张，已退款
  ('O202606140003', 'OTA', '携程旅行', 2, '南湖公园',
   '小张', '小张', '13900000003', NULL,
    19.90, 0.00,  19.90,  19.90, 1,
   '支付宝', '2026-06-14 10:00:00', 'ALI2026061410000567',
   '已退款', '2026-06-14 10:00:30', NULL, NULL,
   '2026-06-14 10:30:00', '2026-06-14 09:55:00',
   '2026-06-14', '2026-06-14', '客户活动取消');

INSERT INTO `order_item`
  (`order_id`, `ticket_id`, `ticket_name`, `scenic_id`, `inventory_id`, `inventory_date`,
   `unit_price`, `quantity`, `subtotal_amount`, `rule_ids`, `discount_amount`, `final_amount`,
   `voucher_codes`, `refund_quantity`, `refund_amount`, `remark`)
VALUES
  -- 1 号单：2 张青秀山成人票
  (1, 1, '青秀山成人票',  1, 6, '2026-06-20', 60.00, 2, 120.00, '1,3', 0.00, 120.00, NULL, 0, 0.00, '在线订单'),
  -- 2 号单：1 张青秀山亲子套票
  (2, 9, '青秀山亲子套票', 1, 6, '2026-06-21', 128.00, 1, 128.00, '1,3', 0.00, 128.00,
   'V2026061409001', 0, 0.00, '1 大 1 小'),
  -- 3 号单：1 张南湖夜跑纪念票
  (3, 7, '南湖夜跑纪念票', 2, NULL, '2026-06-14', 19.90, 1, 19.90, '5', 0.00, 19.90,
   'V2026061409002', 1, 19.90, '已退款');

-- ------------------------------------------------
--  渠道表
--  渠道 = 在线分销通道（OTA / 官网 / 小程序 / APP / 抖音 / 微信 等）
--  每个渠道有独立的佣金比例、对账账户、API 凭据
--  channel_code 是 order 表 channel_code 字段的"主数据"
-- ------------------------------------------------
DROP TABLE IF EXISTS `channel`;
CREATE TABLE `channel` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT            COMMENT '主键',
  `channel_code`      VARCHAR(32)   NOT NULL                           COMMENT '渠道编码（业务主键）',
  `channel_name`      VARCHAR(64)   NOT NULL                           COMMENT '渠道名称',
  `channel_type`      VARCHAR(16)   NOT NULL                           COMMENT '渠道类型：OTA/官网/小程序/APP/短视频/微信/其他',
  `icon`              VARCHAR(16)   DEFAULT NULL                       COMMENT '渠道图标（emoji 或单字）',
  `icon_bg`           VARCHAR(16)   DEFAULT NULL                       COMMENT '渠道图标背景色 HEX',
  `commission_rate`   DECIMAL(5,2)  NOT NULL DEFAULT 0.00              COMMENT '佣金比例（百分比 0-100）',
  `contact_name`      VARCHAR(32)   DEFAULT NULL                       COMMENT '商务联系人',
  `contact_phone`     VARCHAR(32)   DEFAULT NULL                       COMMENT '商务联系电话',
  `settle_account`    VARCHAR(64)   DEFAULT NULL                       COMMENT '结算账户名',
  `settle_bank`       VARCHAR(64)   DEFAULT NULL                       COMMENT '结算开户行',
  `settle_account_no` VARCHAR(64)   DEFAULT NULL                       COMMENT '结算账号',
  `api_key`           VARCHAR(128)  DEFAULT NULL                       COMMENT 'API 密钥（演示用，生产应密文存储）',
  `api_endpoint`      VARCHAR(255)  DEFAULT NULL                       COMMENT 'API 接入地址',
  `order_count`       INT           NOT NULL DEFAULT 0                 COMMENT '历史订单数（冗余）',
  `total_gmv`         DECIMAL(14,2) NOT NULL DEFAULT 0.00              COMMENT '历史 GMV（冗余）',
  `description`       TEXT          DEFAULT NULL                       COMMENT '渠道说明',
  `status`            VARCHAR(16)   NOT NULL DEFAULT '启用'            COMMENT '状态：启用/停用',
  `sort`              INT           NOT NULL DEFAULT 0                 COMMENT '排序值',
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`        DATETIME      DEFAULT NULL                       COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_code` (`channel_code`),
  KEY `idx_channel_type`   (`channel_type`),
  KEY `idx_channel_status` (`status`),
  KEY `idx_channel_deleted`(`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线分销渠道表';

-- ------------------------------------------------
--  渠道结算单表
--  结算单 = 把一段时间内该渠道产生的订单按 GMV-佣金 算出"应付/已付"对账单
--  status 单向迁移：待确认 → 已确认 → 已打款
--  同一渠道同一结算周期不可重复创建（partial unique: channel_id+period）
-- ------------------------------------------------
DROP TABLE IF EXISTS `channel_settlement`;
CREATE TABLE `channel_settlement` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT            COMMENT '主键',
  `settlement_no`     VARCHAR(32)   NOT NULL                           COMMENT '结算单号（业务主键）',
  `channel_id`        BIGINT        NOT NULL                           COMMENT '渠道 ID',
  `channel_code`      VARCHAR(32)   NOT NULL                           COMMENT '渠道编码（冗余）',
  `channel_name`      VARCHAR(64)   NOT NULL                           COMMENT '渠道名称（冗余）',
  `period_start`      DATE          NOT NULL                           COMMENT '结算起始日（含）',
  `period_end`        DATE          NOT NULL                           COMMENT '结算截止日（含）',
  `order_count`       INT           NOT NULL DEFAULT 0                 COMMENT '结算周期内订单数',
  `order_ids`         TEXT          DEFAULT NULL                       COMMENT '结算周期内订单 ID 列表（CSV）',
  `gmv_amount`        DECIMAL(14,2) NOT NULL DEFAULT 0.00              COMMENT 'GMV 总和',
  `refund_amount`     DECIMAL(14,2) NOT NULL DEFAULT 0.00              COMMENT '退款总和',
  `commission_rate`   DECIMAL(5,2)  NOT NULL DEFAULT 0.00              COMMENT '结算时佣金比例（快照）',
  `commission_amount` DECIMAL(14,2) NOT NULL DEFAULT 0.00              COMMENT '佣金金额 = (GMV-退款) × 比例',
  `payable_amount`    DECIMAL(14,2) NOT NULL DEFAULT 0.00              COMMENT '应付园区金额 = (GMV-退款) - 佣金',
  `paid_amount`       DECIMAL(14,2) NOT NULL DEFAULT 0.00              COMMENT '已付金额',
  `status`            VARCHAR(16)   NOT NULL DEFAULT '待确认'          COMMENT '状态：待确认/已确认/已打款/已作废',
  `confirm_time`      DATETIME      DEFAULT NULL                       COMMENT '确认时间',
  `confirm_staff`     VARCHAR(32)   DEFAULT NULL                       COMMENT '确认人',
  `pay_time`          DATETIME      DEFAULT NULL                       COMMENT '打款时间',
  `pay_transaction`   VARCHAR(64)   DEFAULT NULL                       COMMENT '打款流水号',
  `remark`            VARCHAR(255)  DEFAULT NULL                       COMMENT '备注',
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`        DATETIME      DEFAULT NULL                       COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no`     (`settlement_no`),
  KEY `idx_settlement_channel` (`channel_id`),
  KEY `idx_settlement_status`  (`status`),
  KEY `idx_settlement_period`  (`period_start`,`period_end`),
  KEY `idx_settlement_deleted` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道结算单表';

-- ------------------------------------------------
--  渠道种子数据
--  6 个常用渠道（OTA / 官网 / 小程序 / APP / 抖音 / 微信）
--  commission_rate：OTA/短视频 10-15%，官网/小程序 0-3%
-- ------------------------------------------------
INSERT INTO `channel`
  (`channel_code`, `channel_name`, `channel_type`, `icon`, `icon_bg`,
   `commission_rate`, `contact_name`, `contact_phone`,
   `settle_account`, `settle_bank`, `settle_account_no`,
   `api_key`, `api_endpoint`, `order_count`, `total_gmv`,
   `description`, `status`, `sort`)
VALUES
  ('OTA',      '携程旅行',   'OTA',      '🛫', '#2563eb', 12.00, '王经理', '13800000010',
   '携程旅行网有限公司', '工商银行上海分行', '6222021234567890',
   'CTRP-LIVE-KEY-XXXX', 'https://openapi.ctrip.com', 0, 0.00,
   '携程旅行网 - 国内最大 OTA 平台，按月结算',          '启用', 100),
  ('官网',     '青秀山官网', '官网',     '🌐', '#0ea5e9',  0.00, '张运营', '13800000011',
   '南宁青秀山旅游有限公司', '建设银行南宁分行', '6236681234567890',
   NULL, 'https://www.qxs.com', 0, 0.00,
   '景区官方 PC 官网，零佣金',                          '启用',  90),
  ('小程序',   '微信小程序', '小程序',   '💬', '#22c55e',  1.50, '李产品', '13800000012',
   '南宁青秀山旅游有限公司', '招商银行南宁分行', '6225761234567890',
   'WX-MINI-KEY-XXXX', 'https://api.weixin.qq.com', 0, 0.00,
   '微信小程序，支付走微信支付',                        '启用',  80),
  ('APP',      '青秀山 APP', 'APP',      '📱', '#a855f7',  0.00, '陈技术', '13800000013',
   '南宁青秀山旅游有限公司', '中国银行南宁分行', '6217851234567890',
   NULL, 'https://app.qxs.com', 0, 0.00,
   '景区自有 APP',                                       '启用',  70),
  ('抖音',     '抖音生活服务', '短视频', '🎵', '#ef4444',  8.00, '赵商务', '13800000014',
   '北京微播视界科技有限公司', '交通银行北京分行', '6222601234567890',
   'DOUYIN-LIVE-KEY-XXXX', 'https://open.douyin.com', 0, 0.00,
   '抖音生活服务团购券，核销后 T+7 自动结算',           '启用',  60),
  ('微信',     '微信公众号', '微信',     '💚', '#10b981',  1.00, '钱运营', '13800000015',
   '南宁青秀山旅游有限公司', '农业银行南宁分行', '6228481234567890',
   NULL, 'https://mp.weixin.qq.com', 0, 0.00,
   '微信公众号 H5 商城，零佣金',                       '停用',  50);

-- ------------------------------------------------
--  渠道结算单种子数据
--  2 号单已确认未打款、1 号单待确认
--  历史订单按 OTA 渠道（2 号订单）和 官网/小程序（0 号订单无数据）统计
--  实际 GMV 与 commission_amount 数值来自脚本运行时的实时聚合（写死演示值）
-- ------------------------------------------------
INSERT INTO `channel_settlement`
  (`settlement_no`, `channel_id`, `channel_code`, `channel_name`,
   `period_start`, `period_end`,
   `order_count`, `order_ids`,
   `gmv_amount`, `refund_amount`,
   `commission_rate`, `commission_amount`, `payable_amount`, `paid_amount`,
   `status`, `confirm_time`, `confirm_staff`,
   `pay_time`, `pay_transaction`, `remark`)
VALUES
  ('CS202605001', 1, 'OTA', '携程旅行',
   '2026-05-01', '2026-05-31', 0, NULL,
   0.00, 0.00, 12.00, 0.00, 0.00, 0.00,
   '待确认', NULL, NULL, NULL, NULL, '5 月份携程渠道对账'),
  ('CS202606001', 1, 'OTA', '携程旅行',
   '2026-06-01', '2026-06-14', 1, '3',
   19.90, 19.90, 12.00, 0.00, 0.00, 0.00,
   '已确认', '2026-06-14 11:00:00', '财务小李',
   NULL, NULL, '6 月 1-14 日携程对账（订单 3 全额退款）');

-- ------------------------------------------------
--  系统参数表（key-value 全局配置）
--  value_type 决定 settingValue 如何被反序列化：
--    STRING  - 原始字符串
--    NUMBER  - 整数或小数
--    BOOLEAN - true/false
--    JSON    - JSON 对象 / 数组（校验合法性后存）
--  is_readonly=true 表示系统内置、不可修改（如：VERSION / COMPANY_NAME）
--  group_name 用于在管理后台按分组展示
-- ------------------------------------------------
DROP TABLE IF EXISTS `setting`;
CREATE TABLE `setting` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT            COMMENT '主键',
  `setting_key`   VARCHAR(64)   NOT NULL                           COMMENT '参数键（业务主键，唯一）',
  `setting_value` TEXT          DEFAULT NULL                       COMMENT '参数值（原始字符串）',
  `value_type`    VARCHAR(16)   NOT NULL DEFAULT 'STRING'          COMMENT '值类型：STRING/NUMBER/BOOLEAN/JSON',
  `group_name`    VARCHAR(32)   NOT NULL DEFAULT '通用'             COMMENT '参数分组：通用/订单/支付/票务/渠道/园区',
  `description`   VARCHAR(255)  DEFAULT NULL                       COMMENT '参数说明',
  `is_readonly`   TINYINT(1)    NOT NULL DEFAULT 0                 COMMENT '是否只读：1=是（系统内置），0=否',
  `status`        VARCHAR(16)   NOT NULL DEFAULT '启用'            COMMENT '状态：启用/停用',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`    DATETIME      DEFAULT NULL                       COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key`    (`setting_key`),
  KEY `idx_setting_group`   (`group_name`),
  KEY `idx_setting_status`  (`status`),
  KEY `idx_setting_deleted` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';

-- ------------------------------------------------
--  操作日志表（管理后台审计追踪）
--  module + action 描述具体操作（如：园区/新增、订单/退款、票据/作废）
--  biz_id + biz_no 关联业务实体（可空）
--  request_params 与 response_result 按长度限制存储（防止大对象撑爆）
--  status=成功/失败；duration_ms 用于排查慢操作
--  本原型不接 AOP，由业务 Service 显式调用 OpLogService.record() 写入
-- ------------------------------------------------
DROP TABLE IF EXISTS `op_log`;
CREATE TABLE `op_log` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT            COMMENT '主键',
  `module`           VARCHAR(32)   NOT NULL                           COMMENT '操作模块：园区/票种/库存/销售/检票/票据/订单/渠道/系统',
  `action`           VARCHAR(32)   NOT NULL                           COMMENT '操作动作：新增/修改/删除/状态/退款/作废/确认/打款',
  `biz_id`           BIGINT        DEFAULT NULL                       COMMENT '业务实体 ID（可空）',
  `biz_no`           VARCHAR(64)   DEFAULT NULL                       COMMENT '业务流水号（可空，如销售单号/订单号）',
  `operator_id`      BIGINT        DEFAULT NULL                       COMMENT '操作人 ID（占位）',
  `operator_name`    VARCHAR(32)   DEFAULT NULL                       COMMENT '操作人姓名',
  `operator_role`    VARCHAR(32)   DEFAULT NULL                       COMMENT '操作人角色（占位）',
  `request_url`      VARCHAR(255)  DEFAULT NULL                       COMMENT '请求 URL',
  `request_method`   VARCHAR(8)    DEFAULT NULL                       COMMENT 'HTTP 方法：GET/POST/...',
  `request_params`   TEXT          DEFAULT NULL                       COMMENT '请求参数（截断存储）',
  `response_result`  TEXT          DEFAULT NULL                       COMMENT '响应结果（截断存储）',
  `ip`               VARCHAR(64)   DEFAULT NULL                       COMMENT '客户端 IP',
  `user_agent`       VARCHAR(255)  DEFAULT NULL                       COMMENT 'UA',
  `status`           VARCHAR(16)   NOT NULL DEFAULT '成功'            COMMENT '状态：成功/失败',
  `error_msg`        TEXT          DEFAULT NULL                       COMMENT '错误信息（status=失败 时填）',
  `duration_ms`      BIGINT        DEFAULT NULL                       COMMENT '耗时（毫秒）',
  `op_time`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `remark`           VARCHAR(255)  DEFAULT NULL                       COMMENT '备注',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`       DATETIME      DEFAULT NULL                       COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_oplog_module`    (`module`),
  KEY `idx_oplog_action`    (`action`),
  KEY `idx_oplog_operator`  (`operator_id`),
  KEY `idx_oplog_status`    (`status`),
  KEY `idx_oplog_op_time`   (`op_time`),
  KEY `idx_oplog_biz`       (`biz_id`,`biz_no`),
  KEY `idx_oplog_deleted`   (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ------------------------------------------------
--  系统参数种子数据
--  内置 12 条：4 条只读（系统信息）+ 8 条可配（订单/支付/票务/渠道/园区）
--  value_type / setting_value 严格对应
-- ------------------------------------------------
INSERT INTO `setting`
  (`setting_key`, `setting_value`, `value_type`, `group_name`, `description`, `is_readonly`, `status`)
VALUES
  /* 只读 - 系统信息 */
  ('SYS_VERSION',          'v1.0.0',                         'STRING',  '通用', '系统版本号',                    1, '启用'),
  ('SYS_COMPANY_NAME',     '南宁青秀山旅游有限公司',           'STRING',  '通用', '公司全称',                      1, '启用'),
  ('SYS_DEPLOY_ENV',       'dev',                            'STRING',  '通用', '部署环境：dev/test/prod',       1, '启用'),
  ('SYS_OP_LOG_RETENTION', '180',                            'NUMBER',  '通用', '操作日志保留天数',              1, '启用'),
  /* 订单 */
  ('ORDER_TIMEOUT_MIN',    '30',                             'NUMBER',  '订单', '订单超时未支付自动取消（分钟）', 0, '启用'),
  ('ORDER_AUTO_ISSUE',     'true',                           'BOOLEAN', '订单', '创建订单后是否自动出票',         0, '启用'),
  /* 支付 */
  ('PAYMENT_DEFAULT_METHOD', '微信',                         'STRING',  '支付', '默认支付方式',                  0, '启用'),
  ('PAYMENT_METHODS',      '["现金","微信","支付宝","银行卡","余额"]', 'JSON', '支付', '可用的支付方式列表',        0, '启用'),
  /* 票务 */
  ('TICKET_DEFAULT_VALID_DAYS', '1',                         'NUMBER',  '票务', '票种默认有效天数',               0, '启用'),
  ('TICKET_PRINT_COUNT_LIMIT','5',                          'NUMBER',  '票务', '单张票据允许重打的最大次数',     0, '启用'),
  /* 渠道 */
  ('CHANNEL_SETTLE_DAY',   '15',                             'NUMBER',  '渠道', '渠道结算日（每月几日）',         0, '启用'),
  /* 园区 */
  ('SCENIC_DEFAULT_OPEN_TIME', '08:00-18:00',                'STRING',  '园区', '园区默认开放时间',               0, '启用');

-- ------------------------------------------------
--  操作日志种子数据
--  3 条覆盖：成功新增 / 失败 / 状态切换
-- ------------------------------------------------
INSERT INTO `op_log`
  (`module`, `action`, `biz_id`, `biz_no`, `operator_name`, `operator_role`,
   `request_url`, `request_method`, `request_params`, `response_result`,
   `ip`, `user_agent`, `status`, `error_msg`, `duration_ms`, `op_time`, `remark`)
VALUES
  ('园区', '新增', 4, NULL, '管理员A', '运营',
   '/api/scenics', 'POST',
   '{"name":"昆仑关战役旧址","level":"国家4A级景区"}',
   '{"code":200,"message":"操作成功","data":4}',
   '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
   '成功', NULL, 35, '2026-06-14 10:30:00', '新园区上线'),
  ('订单', '退款', 3, 'O202606140003', '财务小李', '财务',
   '/api/orders/3/refund', 'POST',
   '{"reason":"客户活动取消"}',
   '{"code":200,"message":"操作成功"}',
   '127.0.0.1', 'PostmanRuntime/7.36.0',
   '成功', NULL, 22, '2026-06-14 10:30:30', 'OTA 订单全单退'),
  ('票据', '作废', 2, 'V202606140002', '管理员A', '运营',
   '/api/vouchers/revoke', 'POST',
   '{"ids":[2],"reason":"二维码污损","staffName":"管理员A"}',
   '{"code":200,"message":"操作成功","data":1}',
   '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
   '成功', NULL, 18, '2026-06-14 11:00:00', '运营手动作废');

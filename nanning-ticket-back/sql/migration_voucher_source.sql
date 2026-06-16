-- ============================================================
-- Voucher 票据来源拆分迁移
-- ============================================================
-- 历史背景：原型阶段 voucher.sale_id / voucher.sale_item_id 字段
--   同时承载 Sale.id 和 Order.id，存在语义歧义。
-- 修复方案：新增 source_type / order_id / order_item_id 三个字段，
--   source_type='SALE' 时 sale_id/sale_item_id 仍代表销售单/明细；
--   source_type='ORDER' 时使用 order_id/order_item_id。
-- 已存在的 Order 票据可通过 sale_id 字段反查 Order 表识别并补齐。
-- ============================================================

-- 1. 新增列
ALTER TABLE `voucher`
    ADD COLUMN `source_type`   VARCHAR(16) DEFAULT 'SALE'  COMMENT '票据来源：SALE=窗口售票，ORDER=在线订单' AFTER `status`,
    ADD COLUMN `order_id`      BIGINT      DEFAULT NULL    COMMENT '所属订单 ID（sourceType=ORDER 时使用）' AFTER `sale_item_id`,
    ADD COLUMN `order_item_id` BIGINT      DEFAULT NULL    COMMENT '所属订单明细 ID（sourceType=ORDER 时使用）' AFTER `order_id`;

-- 2. 新增索引
ALTER TABLE `voucher`
    ADD KEY `idx_voucher_source`   (`source_type`),
    ADD KEY `idx_voucher_order`    (`order_id`),
    ADD KEY `idx_voucher_orderitem`(`order_item_id`);

-- 3. 兼容历史数据：sale_id 不在 sale 表中说明可能是 order 数据，自动识别
UPDATE `voucher` v
LEFT JOIN `sale` s ON s.id = v.sale_id
SET v.source_type = CASE
    WHEN s.id IS NULL THEN 'ORDER'
    ELSE 'SALE'
END,
v.order_id = CASE
    WHEN s.id IS NULL THEN v.sale_id
    ELSE NULL
END
WHERE v.deleted_at IS NULL;

-- 4. 注：sale_item_id 字段保留并继续承载 OrderItem.id（如有需要可再拆 order_item_id 兼容列）。
--    后续若需严格区分 SaleItem.id 与 OrderItem.id，可根据 source_type 在业务层做映射。

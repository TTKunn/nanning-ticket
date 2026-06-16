package com.ainanning.ticketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 在线订单明细实体
 *
 * <p>对应数据库表：order_item</p>
 *
 * <p>设计要点：
 * <ul>
 *   <li>结构与 {@code sale_item} 高度相似（票种 × 日期 × 数量 + 票据码冗余），独立成表避免跨业务耦合</li>
 *   <li>{@code voucherCodes} 出票时由 {@code VoucherService.issue} 回填（冗余）</li>
 *   <li>{@code refundQuantity} / {@code refundAmount} 累加已退（本原型不支持部分退，预留）</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
@Schema(description = "在线订单明细")
public class OrderItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "所属订单 ID")
    private Long orderId;

    @Schema(description = "票种 ID")
    private Long ticketId;

    @Schema(description = "票种名称（冗余）")
    private String ticketName;

    @Schema(description = "园区 ID（冗余）")
    private Long scenicId;

    @Schema(description = "对应库存记录 ID")
    private Long inventoryId;

    @Schema(description = "入场日期")
    private LocalDate inventoryDate;

    @Schema(description = "销售单价")
    private BigDecimal unitPrice;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "原价小计 = unitPrice * quantity")
    private BigDecimal subtotalAmount;

    @Schema(description = "应用规则 ID（逗号分隔）")
    private String ruleIds;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "实付小计 = subtotal - discount")
    private BigDecimal finalAmount;

    @Schema(description = "票据码（逗号分隔）")
    private String voucherCodes;

    @Schema(description = "已退数量（≤ quantity）")
    private Integer refundQuantity;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    @Schema(description = "备注")
    private String remark;
}

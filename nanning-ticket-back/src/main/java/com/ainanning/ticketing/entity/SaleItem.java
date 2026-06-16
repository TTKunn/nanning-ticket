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
 * 销售明细实体
 *
 * <p>对应数据库表：sale_item</p>
 *
 * <p>设计要点：
 * <ul>
 *   <li>一条明细对应"一种票 × 一个入场日期 × 多个数量"</li>
 *   <li>{@code unitPrice} 为销售单价（取自票种价或活动价，本原型直接取票种价）</li>
 *   <li>{@code refundQuantity} 用于部分退票：累计已退数量，不能超过 {@code quantity}</li>
 *   <li>{@code voucherCodes} 为预留字段，逗号分隔的票据码；先由 Service 自动生成临时码占位，
 *       后续由票据模块替换为正式编码</li>
 *   <li>{@code ruleIds} 冗余自票种，本表不再校验规则存在性（售出时已校验）</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sale_item")
@Schema(description = "窗口销售明细")
public class SaleItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "销售主单 ID")
    private Long saleId;

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

    @Schema(description = "票据码（逗号分隔，预留给票据模块）")
    private String voucherCodes;

    @Schema(description = "已退数量（≤ quantity）")
    private Integer refundQuantity;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    @Schema(description = "备注")
    private String remark;
}

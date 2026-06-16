package com.ainanning.ticketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 渠道结算单实体
 *
 * <p>结算单 = 一段时间内（通常一个月）某渠道产生的订单按"GMV - 退款 - 佣金"算出的应付对账单。
 * 状态机：{@code 待确认 → 已确认 → 已打款}（任一状态都可被作废）</p>
 *
 * <p>关键金额计算（{@code ChannelSettlementServiceImpl.calculate}）：
 * <ul>
 *   <li>佣金金额 = (GMV - 退款) × 佣金比例 / 100</li>
 *   <li>应付金额 = (GMV - 退款) - 佣金</li>
 * </ul>
 * </p>
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("channel_settlement")
public class ChannelSettlement extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /* ===== 状态常量（单向迁移） ===== */
    public static final String STATUS_PENDING  = "待确认";
    public static final String STATUS_CONFIRMED = "已确认";
    public static final String STATUS_PAID     = "已打款";
    public static final String STATUS_CANCEL   = "已作废";

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "结算单号（业务主键）", example = "CS202606001")
    private String settlementNo;

    @Schema(description = "渠道 ID")
    private Long channelId;

    @Schema(description = "渠道编码（冗余）")
    private String channelCode;

    @Schema(description = "渠道名称（冗余）")
    private String channelName;

    @Schema(description = "结算起始日（含）", example = "2026-06-01")
    private LocalDate periodStart;

    @Schema(description = "结算截止日（含）", example = "2026-06-30")
    private LocalDate periodEnd;

    @Schema(description = "结算周期内订单数")
    private Integer orderCount;

    @Schema(description = "结算周期内订单 ID 列表（CSV）")
    @JsonIgnore
    private String orderIds;

    @Schema(description = "GMV 总和")
    private BigDecimal gmvAmount;

    @Schema(description = "退款总和")
    private BigDecimal refundAmount;

    @Schema(description = "结算时佣金比例（快照）")
    private BigDecimal commissionRate;

    @Schema(description = "佣金金额 = (GMV-退款) × 比例 / 100")
    private BigDecimal commissionAmount;

    @Schema(description = "应付园区金额 = (GMV-退款) - 佣金")
    private BigDecimal payableAmount;

    @Schema(description = "已付金额")
    private BigDecimal paidAmount;

    @Schema(description = "状态：待确认/已确认/已打款/已作废", example = "待确认")
    private String status;

    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "确认人")
    private String confirmStaff;

    @Schema(description = "打款时间")
    private LocalDateTime payTime;

    @Schema(description = "打款流水号")
    @JsonIgnore
    private String payTransaction;

    @Schema(description = "备注")
    private String remark;
}

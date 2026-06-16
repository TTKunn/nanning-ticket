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
import java.time.LocalDateTime;

/**
 * 销售主单实体（窗口售票）
 *
 * <p>对应数据库表：sale</p>
 *
 * <p>设计要点：
 * <ul>
 *   <li>一次窗口出票生成一条 {@code sale} 记录，可包含多个 {@code saleItem}（多票种/多日期）</li>
 *   <li>{@code saleNo} 全局唯一，规则 {@code S + yyyyMMdd + 4 位流水}，由 Service 层生成</li>
 *   <li>{@code scenicId} 冗余自首条明细的票种所属园区，便于按园区过滤销售记录</li>
 *   <li>{@code status} 状态机：{@code 已支付 → 部分退票 → 已退票}；{@code 已取消} 为初始未支付回滚态</li>
 *   <li>{@code refundAmount} 在部分退票时累加；全单退票后等于 {@code paidAmount}</li>
 *   <li>售票员、窗口字段在系统/部门模块上线前为占位冗余</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sale")
@Schema(description = "窗口销售主单")
public class Sale extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "销售流水号 e.g. S202606140001")
    private String saleNo;

    @Schema(description = "园区 ID")
    private Long scenicId;

    @Schema(description = "窗口 ID（占位）")
    private Long windowId;

    @Schema(description = "窗口名称（冗余）")
    private String windowName;

    @Schema(description = "售票员 ID（占位）")
    private Long salespersonId;

    @Schema(description = "售票员姓名（冗余）")
    private String salespersonName;

    @Schema(description = "取票/购票人姓名")
    private String visitorName;

    @Schema(description = "联系电话")
    private String visitorPhone;

    @Schema(description = "身份证号")
    private String visitorIdCard;

    @Schema(description = "业务类型：售票/退票")
    private String saleType;

    @Schema(description = "支付方式：现金/微信/支付宝/银行卡/余额")
    private String paymentMethod;

    @Schema(description = "原价合计")
    private BigDecimal totalAmount;

    @Schema(description = "优惠合计")
    private BigDecimal discountAmount;

    @Schema(description = "实付金额（= total - discount）")
    private BigDecimal paidAmount;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    @Schema(description = "票数合计（所有明细 quantity 之和）")
    private Integer itemCount;

    @Schema(description = "状态：已支付/部分退票/已退票/已取消")
    private String status;

    @Schema(description = "交易时间")
    private LocalDateTime saleTime;

    @Schema(description = "备注")
    private String remark;
}

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
import java.time.LocalDateTime;

/**
 * 在线订单实体
 *
 * <p>对应数据库表：order</p>
 *
 * <p>设计要点：
 * <ul>
 *   <li>与窗口销售（{@code sale}）并列：sale 是"现场票台出票"，order 是"在线渠道下单"；
 *       二者业务流不同（order 有支付环节、sale 是现场付清），但最终都生成 voucher 并可核销</li>
 *   <li>{@code orderNo} 全表唯一（uk_order_no），格式：{@code O + yyyyMMddHHmmss + 3 位随机}</li>
 *   <li>{@code status} 状态机：
 *       {@code 待支付 → 已出票（pay 一气呵成）}
 *       {@code 待支付 → 已取消}
 *       {@code 已出票 → 退款中 → 已退款}（refund）
 *       {@code 已出票 → 部分退款}（partial refund，本原型暂不支持）</li>
 *   <li>{@code useStartDate} / {@code useEndDate} 为该订单所有票的入场日期 [min, max]，
 *       冗余便于"按入场日期范围筛选订单"的查询（如：未来 7 天的订单）</li>
 *   <li>{@code refundAmount} 累加已退金额；本原型只支持全单退，部分退预留 ORDER_PARTIAL_REFUND_NOT_SUPPORTED</li>
 *   <li>{@code payMethod/payTime/payTransactionId} 三件套用于对接支付平台（微信/支付宝）的对账</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("`order`")
@Schema(description = "在线订单")
public class Order extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /* 状态枚举 */
    public static final String STATUS_PENDING  = "待支付";
    public static final String STATUS_FULFILLED = "已出票";
    public static final String STATUS_CANCEL   = "已取消";
    public static final String STATUS_REFUNDING = "退款中";
    public static final String STATUS_REFUNDED = "已退款";
    public static final String STATUS_PARTIAL  = "部分退款";

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "订单流水号（唯一）")
    private String orderNo;

    @Schema(description = "渠道编码：OTA/官网/小程序/APP/抖音/微信")
    private String channelCode;

    @Schema(description = "渠道名称（冗余）")
    private String channelName;

    @Schema(description = "园区 ID")
    private Long scenicId;

    @Schema(description = "园区名称（冗余）")
    private String scenicName;

    @Schema(description = "下单用户 ID（占位）")
    private Long userId;

    @Schema(description = "下单用户名（冗余）")
    private String userName;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人手机")
    private String contactPhone;

    @Schema(description = "联系人身份证")
    private String contactIdCard;

    @Schema(description = "订单原价总额")
    private BigDecimal totalAmount;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "实付金额 = total - discount")
    private BigDecimal paidAmount;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    @Schema(description = "票数合计")
    private Integer itemCount;

    @Schema(description = "支付方式：微信/支付宝/银行卡/余额")
    private String payMethod;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "支付平台流水号")
    private String payTransactionId;

    @Schema(description = "订单状态：待支付/已出票/已取消/退款中/已退款/部分退款")
    private String status;

    @Schema(description = "出票时间（生成 voucher 的时间）")
    private LocalDateTime fulfillTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "退款完成时间")
    private LocalDateTime refundTime;

    @Schema(description = "下单时间")
    private LocalDateTime orderTime;

    @Schema(description = "入场起始日期（最早一张票）")
    private LocalDate useStartDate;

    @Schema(description = "入场结束日期（最晚一张票）")
    private LocalDate useEndDate;

    @Schema(description = "备注")
    private String remark;
}

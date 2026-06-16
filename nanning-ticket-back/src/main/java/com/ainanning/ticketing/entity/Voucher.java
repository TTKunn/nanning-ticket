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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 票据实体（一张票一个独立实体）
 *
 * <p>对应数据库表：voucher</p>
 *
 * <p>设计要点：
 * <ul>
 *   <li>每售出 1 张票就生成 1 条 {@code voucher} 记录（区别于 {@code sale_item.voucher_codes} 逗号串），
 *       后续单张作废、补发、核销都可按 ID 精准定位</li>
 *   <li>{@code voucherCode} 全表唯一（uk_voucher_code），支持扫码枪/手输核销反查</li>
 *   <li>{@code status} 状态机：{@code 待使用 → 已使用/已退/已作废}，单向迁移，不可回退</li>
 *   <li>{@code validFrom} / {@code validTo} 由 Service 在出票时根据票种 validDays 计算填入；
 *       检票模块直接读这两个字段判断有效期（不再二次计算）</li>
 *   <li>购票人信息（name/phone/idCard）出票时冗余，便于公安/景区核对</li>
 *   <li>{@code printCount} / {@code lastPrintTime} 便于窗口重打凭据审计</li>
 *   <li>{@code issueTime} 与 {@code createdAt} 解耦：前者可由 Service 显式指定（如批量回填历史数据），
 *       后者由 MyBatis-Plus 自动填充</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("voucher")
@Schema(description = "票据")
public class Voucher extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /* 状态枚举 */
    public static final String STATUS_UNUSED  = "待使用";
    public static final String STATUS_USED    = "已使用";
    public static final String STATUS_REFUND  = "已退";
    public static final String STATUS_REVOKED = "已作废";

    /* 来源枚举 */
    public static final String SOURCE_SALE  = "SALE";  // 窗口售票
    public static final String SOURCE_ORDER = "ORDER"; // 在线订单

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "票据码（唯一）")
    private String voucherCode;

    @Schema(description = "二维码内容/URL（冗余，可与 voucherCode 相同）")
    private String qrCode;

    @Schema(description = "状态：待使用/已使用/已退/已作废")
    private String status;

    /**
     * 票据来源：SALE=窗口售票，ORDER=在线订单
     */
    @Schema(description = "票据来源：SALE=窗口售票，ORDER=在线订单")
    private String sourceType;

    @Schema(description = "所属销售单 ID（sourceType=SALE 时使用）")
    private Long saleId;

    @Schema(description = "所属销售明细 ID（sourceType=SALE 时使用）")
    private Long saleItemId;

    @Schema(description = "所属订单 ID（sourceType=ORDER 时使用）")
    private Long orderId;

    @Schema(description = "所属订单明细 ID（sourceType=ORDER 时使用）")
    private Long orderItemId;

    @Schema(description = "票种 ID")
    private Long ticketId;

    @Schema(description = "票种名称（冗余）")
    private String ticketName;

    @Schema(description = "园区 ID（冗余）")
    private Long scenicId;

    @Schema(description = "园区名称（冗余）")
    private String scenicName;

    @Schema(description = "对应库存记录 ID")
    private Long inventoryId;

    @Schema(description = "入场日期")
    private LocalDate inventoryDate;

    @Schema(description = "入场有效期起")
    private LocalDate validFrom;

    @Schema(description = "入场有效期止")
    private LocalDate validTo;

    @Schema(description = "购票人姓名")
    private String visitorName;

    @Schema(description = "购票人手机")
    private String visitorPhone;

    @Schema(description = "购票人身份证")
    private String visitorIdCard;

    @Schema(description = "核销时间")
    private LocalDateTime useTime;

    @Schema(description = "检票员 ID")
    private Long useStaffId;

    @Schema(description = "检票员姓名（冗余）")
    private String useStaffName;

    @Schema(description = "设备 ID")
    private Long deviceId;

    @Schema(description = "设备名称（冗余）")
    private String deviceName;

    @Schema(description = "打印次数")
    private Integer printCount;

    @Schema(description = "最近打印时间")
    private LocalDateTime lastPrintTime;

    @Schema(description = "出票时间")
    private LocalDateTime issueTime;

    @Schema(description = "作废时间")
    private LocalDateTime revokeTime;

    @Schema(description = "作废原因")
    private String revokeReason;

    @Schema(description = "作废操作员")
    private String revokeStaff;

    @Schema(description = "备注")
    private String remark;
}

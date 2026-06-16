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
 * 检票记录实体
 *
 * <p>对应数据库表：verify_record</p>
 *
 * <p>设计要点：
 * <ul>
 *   <li>每次检票尝试（无论成功/失败）都生成一条记录，便于审计与统计</li>
 *   <li>唯一性保证：同一 {@code voucherCode} 同时仅允许一条 {@code result=成功} 记录；
 *       由 Service 层"先查后插"保证，DB 层不强制 partial unique（MySQL 不支持）</li>
 *   <li>{@code saleId} / {@code saleItemId} / {@code ticketName} 等均为冗余字段，失败记录允许为 NULL</li>
 *   <li>{@code failReason} 在 {@code result=失败} 时必填，枚举值：已使用/已过期/未生效/销售单已退/无效码</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("verify_record")
@Schema(description = "检票记录")
public class VerifyRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "被检票据码")
    private String voucherCode;

    @Schema(description = "所属销售单 ID（失败记录可空）")
    private Long saleId;

    @Schema(description = "所属销售明细 ID（失败记录可空）")
    private Long saleItemId;

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

    @Schema(description = "检票时间")
    private LocalDateTime verifyTime;

    @Schema(description = "检票方式：扫码/手输/刷脸")
    private String verifyMethod;

    @Schema(description = "检票员 ID（占位）")
    private Long verifyStaffId;

    @Schema(description = "检票员姓名（冗余）")
    private String verifyStaffName;

    @Schema(description = "闸机/设备 ID（占位）")
    private Long deviceId;

    @Schema(description = "设备名称（冗余）")
    private String deviceName;

    @Schema(description = "检票结果：成功/失败")
    private String result;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "购票人（冗余）")
    private String visitorName;
}

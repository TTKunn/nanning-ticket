package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.Voucher;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 票据视图对象
 *
 * <p>用于分页查询 / 详情 / 检票结果补全等场景；{@code saleNo} 由 Service 注入。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "票据视图")
public class VoucherVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "票据码")
    private String voucherCode;

    @Schema(description = "二维码内容")
    private String qrCode;

    @Schema(description = "状态：待使用/已使用/已退/已作废")
    private String status;

    @Schema(description = "票据来源：SALE=窗口售票，ORDER=在线订单")
    private String sourceType;

    @Schema(description = "所属销售单 ID（SALE 来源）")
    private Long saleId;

    @Schema(description = "销售流水号（注入）")
    private String saleNo;

    @Schema(description = "所属销售明细 ID（SALE 来源）")
    private Long saleItemId;

    @Schema(description = "所属订单 ID（ORDER 来源）")
    private Long orderId;

    @Schema(description = "订单流水号（注入）")
    private String orderNo;

    @Schema(description = "所属订单明细 ID（ORDER 来源）")
    private Long orderItemId;

    @Schema(description = "票种 ID")
    private Long ticketId;

    @Schema(description = "票种名称")
    private String ticketName;

    @Schema(description = "园区 ID")
    private Long scenicId;

    @Schema(description = "园区名称")
    private String scenicName;

    @Schema(description = "对应库存记录 ID")
    private Long inventoryId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "入场日期")
    private LocalDate inventoryDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "入场有效期起")
    private LocalDate validFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "入场有效期止")
    private LocalDate validTo;

    @Schema(description = "购票人姓名")
    private String visitorName;

    @Schema(description = "购票人手机")
    private String visitorPhone;

    @Schema(description = "购票人身份证")
    private String visitorIdCard;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "核销时间")
    private LocalDateTime useTime;

    @Schema(description = "检票员 ID")
    private Long useStaffId;

    @Schema(description = "检票员姓名")
    private String useStaffName;

    @Schema(description = "设备 ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "打印次数")
    private Integer printCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最近打印时间")
    private LocalDateTime lastPrintTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "出票时间")
    private LocalDateTime issueTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "作废时间")
    private LocalDateTime revokeTime;

    @Schema(description = "作废原因")
    private String revokeReason;

    @Schema(description = "作废操作员")
    private String revokeStaff;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * Entity → VO 转换（{@code saleNo} 由 Service 注入）
     */
    public static VoucherVO from(Voucher entity) {
        if (entity == null) {
            return null;
        }
        return VoucherVO.builder()
                .id(entity.getId())
                .voucherCode(entity.getVoucherCode())
                .qrCode(entity.getQrCode())
                .status(entity.getStatus())
                .sourceType(entity.getSourceType())
                .saleId(entity.getSaleId())
                .saleItemId(entity.getSaleItemId())
                .orderId(entity.getOrderId())
                .orderItemId(entity.getOrderItemId())
                .ticketId(entity.getTicketId())
                .ticketName(entity.getTicketName())
                .scenicId(entity.getScenicId())
                .scenicName(entity.getScenicName())
                .inventoryId(entity.getInventoryId())
                .inventoryDate(entity.getInventoryDate())
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .visitorName(entity.getVisitorName())
                .visitorPhone(entity.getVisitorPhone())
                .visitorIdCard(entity.getVisitorIdCard())
                .useTime(entity.getUseTime())
                .useStaffId(entity.getUseStaffId())
                .useStaffName(entity.getUseStaffName())
                .deviceId(entity.getDeviceId())
                .deviceName(entity.getDeviceName())
                .printCount(entity.getPrintCount())
                .lastPrintTime(entity.getLastPrintTime())
                .issueTime(entity.getIssueTime())
                .revokeTime(entity.getRevokeTime())
                .revokeReason(entity.getRevokeReason())
                .revokeStaff(entity.getRevokeStaff())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

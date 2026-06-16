package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.VerifyRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检票记录视图对象
 *
 * <p>用于分页查询与详情展示；{@code scenicName} / {@code ticketName} 由 Service 注入。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "检票记录视图")
public class VerifyRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "被检票据码")
    private String voucherCode;

    @Schema(description = "所属销售单 ID")
    private Long saleId;

    @Schema(description = "销售流水号（注入）")
    private String saleNo;

    @Schema(description = "所属销售明细 ID")
    private Long saleItemId;

    @Schema(description = "票种 ID")
    private Long ticketId;

    @Schema(description = "票种名称")
    private String ticketName;

    @Schema(description = "园区 ID")
    private Long scenicId;

    @Schema(description = "园区名称（注入）")
    private String scenicName;

    @Schema(description = "对应库存记录 ID")
    private Long inventoryId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "入场日期")
    private LocalDate inventoryDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "检票时间")
    private LocalDateTime verifyTime;

    @Schema(description = "检票方式：扫码/手输/刷脸")
    private String verifyMethod;

    @Schema(description = "检票员 ID")
    private Long verifyStaffId;

    @Schema(description = "检票员姓名")
    private String verifyStaffName;

    @Schema(description = "设备 ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "检票结果：成功/失败")
    private String result;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "购票人")
    private String visitorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * Entity → VO 转换
     *
     * <p>{@code scenicName} / {@code saleNo} 由 Service 注入。</p>
     */
    public static VerifyRecordVO from(VerifyRecord entity) {
        if (entity == null) {
            return null;
        }
        return VerifyRecordVO.builder()
                .id(entity.getId())
                .voucherCode(entity.getVoucherCode())
                .saleId(entity.getSaleId())
                .saleItemId(entity.getSaleItemId())
                .ticketId(entity.getTicketId())
                .ticketName(entity.getTicketName())
                .scenicId(entity.getScenicId())
                .inventoryId(entity.getInventoryId())
                .inventoryDate(entity.getInventoryDate())
                .verifyTime(entity.getVerifyTime())
                .verifyMethod(entity.getVerifyMethod())
                .verifyStaffId(entity.getVerifyStaffId())
                .verifyStaffName(entity.getVerifyStaffName())
                .deviceId(entity.getDeviceId())
                .deviceName(entity.getDeviceName())
                .result(entity.getResult())
                .failReason(entity.getFailReason())
                .visitorName(entity.getVisitorName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

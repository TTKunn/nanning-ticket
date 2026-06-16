package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.Sale;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 销售单视图对象
 *
 * <p>明细列表由 Service 注入（{@code items}），按创建顺序返回。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "销售单视图")
public class SaleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "销售流水号")
    private String saleNo;

    @Schema(description = "园区 ID")
    private Long scenicId;

    @Schema(description = "园区名称")
    private String scenicName;

    @Schema(description = "窗口 ID")
    private Long windowId;

    @Schema(description = "窗口名称")
    private String windowName;

    @Schema(description = "售票员 ID")
    private Long salespersonId;

    @Schema(description = "售票员姓名")
    private String salespersonName;

    @Schema(description = "购票人姓名")
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

    @Schema(description = "实付金额")
    private BigDecimal paidAmount;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    @Schema(description = "票数合计")
    private Integer itemCount;

    @Schema(description = "状态：已支付/部分退票/已退票/已取消")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "交易时间")
    private LocalDateTime saleTime;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "销售明细列表")
    private List<SaleItemVO> items;

    /**
     * Entity → VO 转换（不含明细，明细由 Service 注入）
     */
    public static SaleVO from(Sale entity) {
        if (entity == null) {
            return null;
        }
        return SaleVO.builder()
                .id(entity.getId())
                .saleNo(entity.getSaleNo())
                .scenicId(entity.getScenicId())
                .windowId(entity.getWindowId())
                .windowName(entity.getWindowName())
                .salespersonId(entity.getSalespersonId())
                .salespersonName(entity.getSalespersonName())
                .visitorName(entity.getVisitorName())
                .visitorPhone(entity.getVisitorPhone())
                .visitorIdCard(entity.getVisitorIdCard())
                .saleType(entity.getSaleType())
                .paymentMethod(entity.getPaymentMethod())
                .totalAmount(entity.getTotalAmount())
                .discountAmount(entity.getDiscountAmount())
                .paidAmount(entity.getPaidAmount())
                .refundAmount(entity.getRefundAmount())
                .itemCount(entity.getItemCount())
                .status(entity.getStatus())
                .saleTime(entity.getSaleTime())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .items(Collections.emptyList())
                .build();
    }
}

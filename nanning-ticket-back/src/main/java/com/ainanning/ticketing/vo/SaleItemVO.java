package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.SaleItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销售明细视图对象
 *
 * <p>规则 ID 字符串在 VO 层拆为 List，方便前端直接渲染。
 * <br>{@code availableRefundQty = quantity - refundQuantity} 实时计算。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "销售明细视图")
public class SaleItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "销售主单 ID")
    private Long saleId;

    @Schema(description = "票种 ID")
    private Long ticketId;

    @Schema(description = "票种名称")
    private String ticketName;

    @Schema(description = "园区 ID")
    private Long scenicId;

    @Schema(description = "对应库存记录 ID")
    private Long inventoryId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "入场日期")
    private LocalDate inventoryDate;

    @Schema(description = "销售单价")
    private BigDecimal unitPrice;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "原价小计 = unitPrice * quantity")
    private BigDecimal subtotalAmount;

    @Schema(description = "应用规则 ID")
    private List<Long> ruleIds;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "实付小计 = subtotal - discount")
    private BigDecimal finalAmount;

    @Schema(description = "票据码（逗号分隔）")
    private List<String> voucherCodes;

    @Schema(description = "已退数量")
    private Integer refundQuantity;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    @Schema(description = "可退数量（= quantity - refundQuantity）")
    private Integer availableRefundQty;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * Entity → VO 转换
     *
     * <p>CSV 字段（ruleIds / voucherCodes）在 VO 层拆分为 List；可用退票数量实时计算。</p>
     */
    public static SaleItemVO from(SaleItem entity) {
        if (entity == null) {
            return null;
        }
        int qty         = entity.getQuantity()        == null ? 0 : entity.getQuantity();
        int refundedQty = entity.getRefundQuantity()  == null ? 0 : entity.getRefundQuantity();
        int available   = Math.max(0, qty - refundedQty);

        return SaleItemVO.builder()
                .id(entity.getId())
                .saleId(entity.getSaleId())
                .ticketId(entity.getTicketId())
                .ticketName(entity.getTicketName())
                .scenicId(entity.getScenicId())
                .inventoryId(entity.getInventoryId())
                .inventoryDate(entity.getInventoryDate())
                .unitPrice(entity.getUnitPrice())
                .quantity(qty)
                .subtotalAmount(entity.getSubtotalAmount())
                .ruleIds(splitCsvToLong(entity.getRuleIds()))
                .discountAmount(entity.getDiscountAmount())
                .finalAmount(entity.getFinalAmount())
                .voucherCodes(splitCsvToString(entity.getVoucherCodes()))
                .refundQuantity(refundedQty)
                .refundAmount(entity.getRefundAmount())
                .availableRefundQty(available)
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /** 逗号分隔字符串 → List&lt;Long&gt; */
    private static List<Long> splitCsvToLong(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /** 逗号分隔字符串 → List&lt;String&gt; */
    private static List<String> splitCsvToString(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

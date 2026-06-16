package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单明细视图对象
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "订单明细视图")
public class OrderItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "所属订单 ID")
    private Long orderId;

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

    @Schema(description = "原价小计")
    private BigDecimal subtotalAmount;

    @Schema(description = "应用规则 ID（列表）")
    private List<Long> ruleIds;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "实付小计")
    private BigDecimal finalAmount;

    @Schema(description = "票据码（列表）")
    private List<String> voucherCodes;

    @Schema(description = "可退数量 = quantity - refundQuantity")
    private Integer availableRefundQuantity;

    @Schema(description = "已退数量")
    private Integer refundQuantity;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    @Schema(description = "备注")
    private String remark;

    /**
     * Entity → VO 转换
     *
     * <p>把 CSV 字符串（ruleIds / voucherCodes）拆为 List；计算可退数量。</p>
     */
    public static OrderItemVO from(OrderItem entity) {
        if (entity == null) {
            return null;
        }
        Integer qty = entity.getQuantity() == null ? 0 : entity.getQuantity();
        Integer refunded = entity.getRefundQuantity() == null ? 0 : entity.getRefundQuantity();
        return OrderItemVO.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
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
                .voucherCodes(splitCsv(entity.getVoucherCodes()))
                .availableRefundQuantity(qty - refunded)
                .refundQuantity(refunded)
                .refundAmount(entity.getRefundAmount())
                .remark(entity.getRemark())
                .build();
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

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
}

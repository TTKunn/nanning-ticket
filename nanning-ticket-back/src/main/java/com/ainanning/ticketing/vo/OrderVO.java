package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 订单视图对象
 *
 * <p>用于分页查询 / 详情展示；{@code items} 由 Service 注入。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "订单视图")
public class OrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "订单流水号")
    private String orderNo;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "园区 ID")
    private Long scenicId;

    @Schema(description = "园区名称")
    private String scenicName;

    @Schema(description = "下单用户 ID")
    private Long userId;

    @Schema(description = "下单用户名")
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

    @Schema(description = "实付金额")
    private BigDecimal paidAmount;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    @Schema(description = "票数合计")
    private Integer itemCount;

    @Schema(description = "支付方式")
    private String payMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "支付平台流水号")
    private String payTransactionId;

    @Schema(description = "订单状态")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "出票时间")
    private LocalDateTime fulfillTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "退款完成时间")
    private LocalDateTime refundTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "下单时间")
    private LocalDateTime orderTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "入场起始日期")
    private LocalDate useStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "入场结束日期")
    private LocalDate useEndDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "订单明细列表（注入）")
    private List<OrderItemVO> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * Entity → VO 转换（{@code items} 由 Service 注入）
     */
    public static OrderVO from(Order entity) {
        if (entity == null) {
            return null;
        }
        return OrderVO.builder()
                .id(entity.getId())
                .orderNo(entity.getOrderNo())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .scenicId(entity.getScenicId())
                .scenicName(entity.getScenicName())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .contactName(entity.getContactName())
                .contactPhone(entity.getContactPhone())
                .contactIdCard(entity.getContactIdCard())
                .totalAmount(entity.getTotalAmount())
                .discountAmount(entity.getDiscountAmount())
                .paidAmount(entity.getPaidAmount())
                .refundAmount(entity.getRefundAmount())
                .itemCount(entity.getItemCount())
                .payMethod(entity.getPayMethod())
                .payTime(entity.getPayTime())
                .payTransactionId(entity.getPayTransactionId())
                .status(entity.getStatus())
                .fulfillTime(entity.getFulfillTime())
                .cancelTime(entity.getCancelTime())
                .cancelReason(entity.getCancelReason())
                .refundTime(entity.getRefundTime())
                .orderTime(entity.getOrderTime())
                .useStartDate(entity.getUseStartDate())
                .useEndDate(entity.getUseEndDate())
                .remark(entity.getRemark())
                .items(Collections.emptyList())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

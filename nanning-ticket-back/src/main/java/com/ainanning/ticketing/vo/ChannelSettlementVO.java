package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.ChannelSettlement;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
 * 渠道结算单视图
 *
 * <p>在 Entity 基础上增加 {@code orderIdList}（CSV → List&lt;Long&gt;）的解析，
 * 便于前端直接展示订单编号集合。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "渠道结算单视图")
public class ChannelSettlementVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "结算单号")
    private String settlementNo;

    @Schema(description = "渠道 ID")
    private Long channelId;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "渠道名称")
    private String channelName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结算起始日（含）")
    private LocalDate periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结算截止日（含）")
    private LocalDate periodEnd;

    @Schema(description = "结算周期内订单数")
    private Integer orderCount;

    @Schema(description = "结算周期内订单 ID 列表（解析自 orderIds CSV）")
    private List<Long> orderIdList;

    @Schema(description = "GMV 总和")
    private BigDecimal gmvAmount;

    @Schema(description = "退款总和")
    private BigDecimal refundAmount;

    @Schema(description = "结算时佣金比例（快照）")
    private BigDecimal commissionRate;

    @Schema(description = "佣金金额")
    private BigDecimal commissionAmount;

    @Schema(description = "应付园区金额")
    private BigDecimal payableAmount;

    @Schema(description = "已付金额")
    private BigDecimal paidAmount;

    @Schema(description = "状态：待确认/已确认/已打款/已作废")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "确认人")
    private String confirmStaff;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "打款时间")
    private LocalDateTime payTime;

    @Schema(description = "打款流水号")
    private String payTransaction;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * Entity → VO 转换
     */
    public static ChannelSettlementVO from(ChannelSettlement entity) {
        if (entity == null) {
            return null;
        }
        List<Long> orderIdList = null;
        if (entity.getOrderIds() != null && !entity.getOrderIds().isBlank()) {
            try {
                orderIdList = Arrays.stream(entity.getOrderIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                orderIdList = Collections.emptyList();
            }
        }
        return ChannelSettlementVO.builder()
                .id(entity.getId())
                .settlementNo(entity.getSettlementNo())
                .channelId(entity.getChannelId())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .orderCount(entity.getOrderCount())
                .orderIdList(orderIdList)
                .gmvAmount(entity.getGmvAmount())
                .refundAmount(entity.getRefundAmount())
                .commissionRate(entity.getCommissionRate())
                .commissionAmount(entity.getCommissionAmount())
                .payableAmount(entity.getPayableAmount())
                .paidAmount(entity.getPaidAmount())
                .status(entity.getStatus())
                .confirmTime(entity.getConfirmTime())
                .confirmStaff(entity.getConfirmStaff())
                .payTime(entity.getPayTime())
                .payTransaction(entity.getPayTransaction())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

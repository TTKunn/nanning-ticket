package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.Channel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 渠道视图
 *
 * <p>默认对外隐藏结算账户 / API 密钥（{@code @JsonIgnore}），
 * 财务详情可调用 {@code getById} 在脱敏中间件中按权限放开。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "渠道视图")
public class ChannelVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "渠道类型")
    private String channelType;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "图标背景色")
    private String iconBg;

    @Schema(description = "佣金比例（百分比 0-100）")
    private BigDecimal commissionRate;

    @Schema(description = "商务联系人")
    private String contactName;

    @Schema(description = "商务联系电话")
    private String contactPhone;

    @JsonIgnore
    @Schema(description = "结算账户名（敏感）", hidden = true)
    private String settleAccount;

    @JsonIgnore
    @Schema(description = "结算开户行（敏感）", hidden = true)
    private String settleBank;

    @JsonIgnore
    @Schema(description = "结算账号（敏感）", hidden = true)
    private String settleAccountNo;

    @JsonIgnore
    @Schema(description = "API 密钥（敏感）", hidden = true)
    private String apiKey;

    @Schema(description = "API 接入地址")
    private String apiEndpoint;

    @Schema(description = "历史订单数（冗余）")
    private Integer orderCount;

    @Schema(description = "历史 GMV（冗余）")
    private BigDecimal totalGmv;

    @Schema(description = "渠道说明")
    private String description;

    @Schema(description = "状态：启用/停用")
    private String status;

    @Schema(description = "排序值")
    private Integer sort;

    /**
     * Entity → VO 转换
     */
    public static ChannelVO from(Channel entity) {
        if (entity == null) {
            return null;
        }
        return ChannelVO.builder()
                .id(entity.getId())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .channelType(entity.getChannelType())
                .icon(entity.getIcon())
                .iconBg(entity.getIconBg())
                .commissionRate(entity.getCommissionRate())
                .contactName(entity.getContactName())
                .contactPhone(entity.getContactPhone())
                .settleAccount(entity.getSettleAccount())
                .settleBank(entity.getSettleBank())
                .settleAccountNo(entity.getSettleAccountNo())
                .apiKey(entity.getApiKey())
                .apiEndpoint(entity.getApiEndpoint())
                .orderCount(entity.getOrderCount())
                .totalGmv(entity.getTotalGmv())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .sort(entity.getSort())
                .build();
    }
}

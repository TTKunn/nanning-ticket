package com.ainanning.ticketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 渠道实体
 *
 * <p>渠道 = 在线分销通道（OTA / 官网 / 小程序 / APP / 抖音 / 微信 等）。
 * 订单表 {@code order.channel_code} 是对 {@code channel.channel_code} 的弱引用，
 * 这里定义为主数据，由 channel 模块统一管理。</p>
 *
 * <p>与 order.channel_code 的关系：
 * <ul>
 *   <li>原型早期 order 直接硬编码渠道枚举（OTA/官网/小程序/APP/抖音/微信）</li>
 *   <li>本模块将"主数据"抽到 channel 表，未来 Order 模块会改"channel_id" 关联</li>
 *   <li>本原型不强制改 Order 模块，{@code channel_code} 仍然兼容</li>
 * </ul>
 * </p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("channel")
public class Channel extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /* ===== 状态常量 ===== */
    public static final String STATUS_ENABLED  = "启用";
    public static final String STATUS_DISABLED = "停用";

    /* ===== 类型常量 ===== */
    public static final String TYPE_OTA      = "OTA";
    public static final String TYPE_OFFICIAL = "官网";
    public static final String TYPE_MINI     = "小程序";
    public static final String TYPE_APP      = "APP";
    public static final String TYPE_SHORT    = "短视频";
    public static final String TYPE_WECHAT   = "微信";
    public static final String TYPE_OTHER    = "其他";

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "渠道编码（业务主键，唯一）", example = "OTA")
    private String channelCode;

    @Schema(description = "渠道名称", example = "携程旅行")
    private String channelName;

    @Schema(description = "渠道类型", example = "OTA")
    private String channelType;

    @Schema(description = "渠道图标")
    private String icon;

    @Schema(description = "渠道图标背景色 HEX")
    @TableField("icon_bg")
    private String iconBg;

    @Schema(description = "佣金比例（百分比 0-100）")
    private BigDecimal commissionRate;

    @Schema(description = "商务联系人")
    private String contactName;

    @Schema(description = "商务联系电话")
    private String contactPhone;

    @Schema(description = "结算账户名")
    @JsonIgnore
    private String settleAccount;

    @Schema(description = "结算开户行")
    @JsonIgnore
    private String settleBank;

    @Schema(description = "结算账号")
    @JsonIgnore
    private String settleAccountNo;

    @Schema(description = "API 密钥（演示用，生产应密文存储）")
    @JsonIgnore
    private String apiKey;

    @Schema(description = "API 接入地址")
    private String apiEndpoint;

    @Schema(description = "历史订单数（冗余）")
    private Integer orderCount;

    @Schema(description = "历史 GMV（冗余）")
    private BigDecimal totalGmv;

    @Schema(description = "渠道说明")
    private String description;

    @Schema(description = "状态：启用/停用", example = "启用")
    private String status;

    @Schema(description = "排序值（越大越靠前）")
    private Integer sort;
}

package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 渠道下拉选项
 *
 * <p>供前端 Select / Cascader 使用，仅返回 id / code / name 三字段。
 * 默认只取"启用"状态，按 sort 倒序。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "渠道下拉选项")
public class ChannelOptionVO implements Serializable {

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
}

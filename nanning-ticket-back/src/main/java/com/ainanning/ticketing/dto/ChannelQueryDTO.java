package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 渠道分页查询条件
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "渠道分页查询条件")
public class ChannelQueryDTO extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关键字（按编码 / 名称 模糊）")
    private String keyword;

    @Schema(description = "渠道类型")
    private String channelType;

    @Schema(description = "状态：启用/停用")
    private String status;
}

package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目规则分页查询参数
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "规则查询参数")
public class RuleQueryDTO extends PageQuery {

    @Schema(description = "所属园区 ID（必传）")
    private Long scenicId;

    @Schema(description = "关键字（匹配规则名称 / 编码，模糊）")
    private String keyword;

    @Schema(description = "类型：折扣/免票/团体/时段/限流")
    private String type;

    @Schema(description = "状态：启用/禁用")
    private String status;
}

package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 票种分页查询参数
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "票种查询参数")
public class TicketQueryDTO extends PageQuery {

    @Schema(description = "所属园区 ID（必传）")
    private Long scenicId;

    @Schema(description = "关键字（匹配票种名称 / 编码，模糊）")
    private String keyword;

    @Schema(description = "分类：单票/套票/联票")
    private String category;

    @Schema(description = "状态：在售/停售")
    private String status;
}

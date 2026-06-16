package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区分页查询参数
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "园区查询参数")
public class ScenicQueryDTO extends PageQuery {

    @Schema(description = "园区名称（模糊搜索）")
    private String keyword;

    @Schema(description = "状态：运营中 / 暂停运营")
    private String status;
}

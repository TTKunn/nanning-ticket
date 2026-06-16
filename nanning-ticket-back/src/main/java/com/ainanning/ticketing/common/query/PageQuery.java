package com.ainanning.ticketing.common.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用分页查询参数
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "分页参数")
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于 0")
    private Long pageNum = 1L;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于 0")
    @Max(value = 100, message = "每页大小不能超过 100")
    private Long pageSize = 10L;
}

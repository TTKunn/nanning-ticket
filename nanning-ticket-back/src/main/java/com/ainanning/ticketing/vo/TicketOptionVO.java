package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 票种下拉选项
 *
 * <p>用于售票窗口、订单创建等场景快速选择票种。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "票种下拉选项")
public class TicketOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "票种 ID")
    private Long id;

    @Schema(description = "票种名称")
    private String name;

    @Schema(description = "票种编码")
    private String code;

    @Schema(description = "票面价")
    private BigDecimal price;

    @Schema(description = "所属园区 ID")
    private Long scenicId;

    @Schema(description = "所属园区名称")
    private String scenicName;

    @Schema(description = "分类：单票/套票/联票")
    private String category;
}

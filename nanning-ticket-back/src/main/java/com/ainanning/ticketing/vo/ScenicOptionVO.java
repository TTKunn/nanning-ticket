package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 园区下拉选项（仅含 id / name / icon）
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "园区下拉选项")
public class ScenicOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "园区 ID")
    private Long id;

    @Schema(description = "园区名称")
    private String name;

    @Schema(description = "图标")
    private String icon;
}

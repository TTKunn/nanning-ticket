package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 规则下拉选项（仅含 id / name / type / code）
 *
 * <p>用于票种管理等模块选择适用的规则时使用。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "规则下拉选项")
public class RuleOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "规则 ID")
    private Long id;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则编码")
    private String code;

    @Schema(description = "类型：折扣/免票/团体/时段/限流")
    private String type;
}

package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 渠道状态切换 DTO
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "渠道状态切换参数")
public class ChannelStatusDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "状态不能为空")
    @Schema(description = "状态：启用/停用", example = "启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;
}

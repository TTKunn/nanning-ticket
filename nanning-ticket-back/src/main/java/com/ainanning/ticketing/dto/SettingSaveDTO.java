package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统参数新增 / 修改参数
 *
 * <p>id 为空 = 新增；非空 = 修改。
 * 修改时会校验 {@code isReadonly}：只读参数业务侧调用 {@code save()} 时直接拒绝。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "系统参数新增/修改参数")
public class SettingSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（修改时必填）")
    private Long id;

    @NotBlank(message = "参数键不能为空")
    @Size(max = 64, message = "参数键不能超过 64 个字符")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_.]{1,63}$",
            message = "参数键必须以字母开头，仅含字母数字下划线点")
    @Schema(description = "参数键（业务主键）", example = "ORDER_TIMEOUT_MIN",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String settingKey;

    @NotBlank(message = "参数值不能为空")
    @Size(max = 4096, message = "参数值不能超过 4096 个字符")
    @Schema(description = "参数值（原始字符串，反序列化时按 valueType 校验）",
            example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    private String settingValue;

    @NotBlank(message = "值类型不能为空")
    @Schema(description = "值类型：STRING/NUMBER/BOOLEAN/JSON", example = "NUMBER",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String valueType;

    @NotBlank(message = "参数分组不能为空")
    @Schema(description = "参数分组", example = "订单",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String groupName;

    @Size(max = 255, message = "参数说明不能超过 255 个字符")
    @Schema(description = "参数说明")
    private String description;

    @Schema(description = "是否只读：1=是，0=否（仅修改时生效，新增按需传）")
    private Integer isReadonly;

    @Schema(description = "状态：启用/停用", example = "启用")
    private String status;
}

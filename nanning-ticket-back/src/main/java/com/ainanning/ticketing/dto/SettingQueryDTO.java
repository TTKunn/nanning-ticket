package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统参数分页查询
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统参数分页查询")
public class SettingQueryDTO extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关键字（按 settingKey / description 模糊）")
    private String keyword;

    @Schema(description = "参数分组")
    private String groupName;

    @Schema(description = "值类型：STRING/NUMBER/BOOLEAN/JSON")
    private String valueType;

    @Schema(description = "状态：启用/停用")
    private String status;

    @Schema(description = "是否只读：1=是，0=否")
    private Integer isReadonly;
}

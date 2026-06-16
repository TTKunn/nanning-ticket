package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.Setting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统参数视图
 *
 * <p>在 Entity 基础上对 valueType / isReadonly 等字段做"前端友好"转换：
 * <ul>
 *   <li>{@code valueType} / {@code isReadonly} 原始枚举值原样返回</li>
 *   <li>不展示 createdAt / updatedAt 等基础字段（前端不在此页面关心）</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "系统参数视图")
public class SettingVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "参数键")
    private String settingKey;

    @Schema(description = "参数值")
    private String settingValue;

    @Schema(description = "值类型：STRING/NUMBER/BOOLEAN/JSON")
    private String valueType;

    @Schema(description = "参数分组")
    private String groupName;

    @Schema(description = "参数说明")
    private String description;

    @Schema(description = "是否只读：1=是，0=否")
    private Integer isReadonly;

    @Schema(description = "状态：启用/停用")
    private String status;

    /**
     * Entity → VO
     */
    public static SettingVO from(Setting entity) {
        if (entity == null) return null;
        return SettingVO.builder()
                .id(entity.getId())
                .settingKey(entity.getSettingKey())
                .settingValue(entity.getSettingValue())
                .valueType(entity.getValueType())
                .groupName(entity.getGroupName())
                .description(entity.getDescription())
                .isReadonly(entity.getIsReadonly())
                .status(entity.getStatus())
                .build();
    }
}

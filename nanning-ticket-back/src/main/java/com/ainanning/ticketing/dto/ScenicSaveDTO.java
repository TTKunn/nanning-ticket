package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 园区新增 / 修改参数
 *
 * <p>id 为空时表示新增，非空时表示修改。
 * 月销售/项目数等冗余字段不在此处暴露。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "园区新增/修改参数")
public class ScenicSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（修改时必填）")
    private Long id;

    @NotBlank(message = "园区名称不能为空")
    @Size(max = 64, message = "园区名称不能超过 64 个字符")
    @Schema(description = "园区名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "园区图标（单字或 emoji）")
    private String icon;

    @Schema(description = "园区图标背景色 HEX")
    private String iconBg;

    @Size(max = 32, message = "景区等级不能超过 32 个字符")
    @Schema(description = "景区等级")
    private String level;

    @Size(max = 255, message = "详细地址不能超过 255 个字符")
    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "开放时间（例：08:00-18:00）")
    private String openTime;

    @Schema(description = "园区说明")
    private String description;

    @Schema(description = "状态：运营中 / 暂停运营")
    private String status;

    @Schema(description = "排序值，越大越靠前")
    private Integer sort;
}

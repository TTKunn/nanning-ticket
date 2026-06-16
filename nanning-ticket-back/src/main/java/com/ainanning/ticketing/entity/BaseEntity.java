package com.ainanning.ticketing.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 *
 * <p>统一管理 createdAt / updatedAt / deletedAt。
 * 软删除：本项目使用 deleted_at DATETIME，由各业务自行在 wrapper 中过滤（{@code isNull(DeletedAt)}），
 * 不使用 {@code @TableLogic}（该注解主要支持整型字段）。</p>
 *
 * @author nanning-ticket
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "创建时间", hidden = true)
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", hidden = true)
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Schema(description = "软删除时间", hidden = true)
    @JsonIgnore
    @TableField(value = "deleted_at", select = false)
    private LocalDateTime deletedAt;
}

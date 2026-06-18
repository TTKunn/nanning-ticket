package com.ainanning.ticketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 批量操作结果
 *
 * <p>用于批量更新 / 删除场景，前端可据此展示"成功 N 条 / 跳过 N 条（原因...）"。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "批量操作结果")
public class BatchOpResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "成功处理条数")
    private Integer successCount;

    @Schema(description = "跳过条数（受业务规则限制未处理）")
    private Integer skipCount;

    /** 跳过的日期 → 原因，例如 "2026-07-01" -> "已存在销售记录" */
    @Schema(description = "跳过的明细（日期 → 原因）")
    private Map<String, String> skipped;

    @Schema(description = "跳过的库存 ID 列表（按行 ID）")
    private List<Long> skippedIds;
}

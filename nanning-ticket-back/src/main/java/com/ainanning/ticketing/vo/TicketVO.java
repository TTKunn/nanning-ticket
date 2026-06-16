package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 票种视图对象（用于 API 响应）
 *
 * <p>前端数组形式字段（ruleIds / tags）以 {@code List} 形式返回，
 * 由 Service 层在转换时与数据库的逗号分隔字符串互转。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "票种视图")
public class TicketVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "所属园区 ID")
    private Long scenicId;

    @Schema(description = "所属园区名称")
    private String scenicName;

    @Schema(description = "票种名称")
    private String name;

    @Schema(description = "票种编码")
    private String code;

    @Schema(description = "分类：单票/套票/联票")
    private String category;

    @Schema(description = "票面价")
    private BigDecimal price;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "票种说明")
    private String description;

    @Schema(description = "封面图 URL")
    private String cover;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "入场有效天数")
    private Integer validDays;

    @Schema(description = "是否可退")
    private Boolean refundable;

    @Schema(description = "关联规则 ID 列表")
    private List<Long> ruleIds;

    @Schema(description = "关联规则名称列表（用于展示）")
    private List<String> ruleNames;

    @Schema(description = "状态：在售/停售")
    private String status;

    @Schema(description = "排序值")
    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * Entity → VO 转换
     *
     * <p>scenicName / ruleNames 由 Service 单独注入，这里只做基础字段映射。
     * tags 与 ruleIds 在此做逗号分隔字符串 → List 的转换，避免污染 Entity。</p>
     */
    public static TicketVO from(Ticket entity) {
        if (entity == null) {
            return null;
        }
        return TicketVO.builder()
                .id(entity.getId())
                .scenicId(entity.getScenicId())
                .name(entity.getName())
                .code(entity.getCode())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .costPrice(entity.getCostPrice())
                .description(entity.getDescription())
                .cover(entity.getCover())
                .tags(splitToList(entity.getTags()))
                .validDays(entity.getValidDays())
                .refundable(entity.getRefundable() != null && entity.getRefundable() == 1)
                .ruleIds(parseLongList(entity.getRuleIds()))
                .ruleNames(Collections.emptyList())
                .status(entity.getStatus())
                .sort(entity.getSort())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /** 逗号分隔字符串 → List<String>（空串/空白视作空列表） */
    private static List<String> splitToList(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        String[] parts = csv.split(",");
        return java.util.Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    /** 逗号分隔字符串 → List<Long>（解析失败抛出业务异常） */
    private static List<Long> parseLongList(String csv) {
        List<String> parts = splitToList(csv);
        if (parts.isEmpty()) {
            return Collections.emptyList();
        }
        return parts.stream()
                .map(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (NumberFormatException e) {
                        throw new com.ainanning.ticketing.common.exception.BusinessException(
                                com.ainanning.ticketing.common.result.ResultCode.PARAM_ERROR,
                                "ruleIds 格式错误: " + s);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }
}

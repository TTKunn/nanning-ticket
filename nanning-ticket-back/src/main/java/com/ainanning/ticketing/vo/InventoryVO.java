package com.ainanning.ticketing.vo;

import com.ainanning.ticketing.entity.Inventory;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存视图对象（用于 API 响应）
 *
 * <p>available 在 VO 中实时计算（{@code = total - sold - reserved}），不依赖 DB 字段值，
 * 防止数据漂移；status 在售罄时也实时调整（除非是手动关闭）。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "库存视图")
public class InventoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "票种 ID")
    private Long ticketId;

    @Schema(description = "票种名称")
    private String ticketName;

    @Schema(description = "所属园区 ID")
    private Long scenicId;

    @Schema(description = "所属园区名称")
    private String scenicName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "库存日期")
    private LocalDate inventoryDate;

    @Schema(description = "总库存")
    private Integer total;

    @Schema(description = "已售数量")
    private Integer sold;

    @Schema(description = "预占数量（未付款）")
    private Integer reserved;

    @Schema(description = "可用库存（= total - sold - reserved）")
    private Integer available;

    @Schema(description = "状态：开放/关闭/售罄")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /** 状态常量（保持与 Service 一致） */
    private static final String STATUS_OPEN    = "开放";
    private static final String STATUS_SOLDOUT = "售罄";

    /**
     * Entity → VO 转换
     *
     * <p>available 实时计算：{@code max(0, total - sold - reserved)}。
     * status 也实时调整：若为开放且可用 = 0，则展示为售罄。
     * scenicName / ticketName 由 Service 注入。</p>
     */
    public static InventoryVO from(Inventory entity) {
        if (entity == null) {
            return null;
        }
        int total     = entity.getTotal()     == null ? 0 : entity.getTotal();
        int sold      = entity.getSold()      == null ? 0 : entity.getSold();
        int reserved  = entity.getReserved()  == null ? 0 : entity.getReserved();
        int available = Math.max(0, total - sold - reserved);

        String status = entity.getStatus();
        // 开放 + 已售完 → 显示为售罄
        if (STATUS_OPEN.equals(status) && available == 0) {
            status = STATUS_SOLDOUT;
        }

        return InventoryVO.builder()
                .id(entity.getId())
                .ticketId(entity.getTicketId())
                .scenicId(entity.getScenicId())
                .inventoryDate(entity.getInventoryDate())
                .total(total)
                .sold(sold)
                .reserved(reserved)
                .available(available)
                .status(status)
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

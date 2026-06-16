package com.ainanning.ticketing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检票结果视图
 *
 * <p>闸机/手持终端调 {@code POST /api/verifies} 时直接返回该 VO；
 * <br>无论成功失败，HTTP 状态码均为 200，失败原因在 {@code result} + {@code failReason} 体现。</p>
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "检票结果")
public class VerifyResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 票据来源：窗口售票 */
    public static final String SOURCE_SALE = "SALE";
    /** 票据来源：在线订单 */
    public static final String SOURCE_ORDER = "ORDER";

    @Schema(description = "本次检票记录 ID")
    private Long recordId;

    @Schema(description = "检票结果：成功/失败", example = "成功")
    private String result;

    @Schema(description = "失败原因（成功时为 null）", example = "已使用")
    private String failReason;

    /* ============ 成功时返回的票据信息 ============ */

    @Schema(description = "票据码")
    private String voucherCode;

    /**
     * 票据来源：
     * <ul>
     *   <li>{@code SALE} - 窗口售票（{@link #saleId} / {@link #saleNo} / {@link #saleItemId} 有值）</li>
     *   <li>{@code ORDER} - 在线订单（{@link #orderId} / {@link #orderNo} / {@link #orderItemId} 有值）</li>
     * </ul>
     */
    @Schema(description = "票据来源：SALE=窗口售票，ORDER=在线订单")
    private String sourceType;

    @Schema(description = "销售流水号（窗口售票场景）")
    private String saleNo;

    @Schema(description = "销售单 ID（窗口售票场景）")
    private Long saleId;

    @Schema(description = "销售明细 ID（窗口售票场景）")
    private Long saleItemId;

    @Schema(description = "订单流水号（在线订单场景）")
    private String orderNo;

    @Schema(description = "订单 ID（在线订单场景）")
    private Long orderId;

    @Schema(description = "订单明细 ID（在线订单场景）")
    private Long orderItemId;

    @Schema(description = "票种 ID")
    private Long ticketId;

    @Schema(description = "票种名称")
    private String ticketName;

    @Schema(description = "园区 ID")
    private Long scenicId;

    @Schema(description = "园区名称")
    private String scenicName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "入场日期")
    private LocalDate inventoryDate;

    @Schema(description = "销售单价（核销金额）")
    private BigDecimal unitPrice;

    @Schema(description = "购票人")
    private String visitorName;

    /* ============ 检票过程信息 ============ */

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "检票时间")
    private LocalDateTime verifyTime;

    @Schema(description = "检票方式")
    private String verifyMethod;

    @Schema(description = "检票员姓名")
    private String verifyStaffName;

    @Schema(description = "设备名称")
    private String deviceName;
}

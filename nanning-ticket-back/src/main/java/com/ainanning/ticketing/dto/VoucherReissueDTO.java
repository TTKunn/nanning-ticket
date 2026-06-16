package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 票据补发 DTO
 *
 * <p>对于已退/已作废的票据，重新生成新码（voucher_code + status=待使用），
 * 原票保留以做审计（不删除、不复用 code）。</p>
 *
 * <p>典型场景：客户票丢失、纸质票损毁、扫码设备无法识别码字符等。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "票据补发参数")
public class VoucherReissueDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "被补发的原票据 ID 列表（已退/已作废）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "原票据 ID 列表不能为空")
    @Size(max = 500, message = "单次补发不能超过 500 张")
    private List<Long> sourceIds;

    @Schema(description = "补发原因", example = "客户票丢失")
    private String reason;

    @Schema(description = "操作员姓名")
    private String staffName;
}

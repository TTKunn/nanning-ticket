package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 票据批量作废 DTO
 *
 * <p>用于管理端一键作废一批票据（损坏、二维码被遮蔽、错发等场景），
 * 区别于"退票"：作废是管理行为，退票是销售流水的反向回滚。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "票据批量作废参数")
public class VoucherRevokeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "待作废票据 ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "票据 ID 列表不能为空")
    @Size(max = 500, message = "单次作废不能超过 500 张")
    private List<Long> ids;

    @Schema(description = "作废原因", example = "二维码污损")
    private String reason;

    @Schema(description = "操作员姓名", example = "管理员A")
    private String staffName;
}

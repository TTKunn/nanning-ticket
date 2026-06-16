package com.ainanning.ticketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 票据重打 / 标记打印 DTO
 *
 * <p>用于窗口重打凭据场景：每次重打 printCount 自增 1，
 * 供后续追溯纸质票据发放次数。</p>
 *
 * @author nanning-ticket
 */
@Data
@Schema(description = "票据打印次数累加参数")
public class VoucherMarkPrintedDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "票据 ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "票据 ID 列表不能为空")
    private List<Long> ids;
}

package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.VerifyQueryDTO;
import com.ainanning.ticketing.dto.VerifyRequestDTO;
import com.ainanning.ticketing.service.VerifyService;
import com.ainanning.ticketing.vo.VerifyRecordVO;
import com.ainanning.ticketing.vo.VerifyResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 检票 Controller
 *
 * <p>路由前缀：/api/verifies
 * <br>核心接口 {@code POST /} 供闸机/手持终端调用，输出 {@link VerifyResultVO}。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "06. 检票", description = "票据核销：扫码/手输入场，记录审计")
@Validated
@RestController
@RequestMapping("/api/verifies")
@RequiredArgsConstructor
public class VerifyController {

    private final VerifyService verifyService;

    @Operation(summary = "检票（闸机/终端调用）",
            description = "返回 VerifyResultVO，result=成功 时放行；result=失败 时 failReason 给出原因。" +
                    "无论成功失败都写入审计表。")
    @PostMapping
    public Result<VerifyResultVO> verify(@Valid @RequestBody VerifyRequestDTO dto) {
        return Result.success(verifyService.verify(dto));
    }

    @Operation(summary = "分页查询检票记录")
    @GetMapping
    public Result<PageVO<VerifyRecordVO>> page(@Valid VerifyQueryDTO query) {
        return Result.success(verifyService.page(query));
    }

    @Operation(summary = "获取检票记录详情")
    @GetMapping("/{id}")
    public Result<VerifyRecordVO> getById(
            @Parameter(description = "检票记录 ID") @PathVariable Long id) {
        return Result.success(verifyService.getById(id));
    }

    @Operation(summary = "按票据码查询检票历史（成功 + 失败）")
    @GetMapping("/by-code")
    public Result<List<VerifyRecordVO>> listByVoucherCode(
            @Parameter(description = "票据码", example = "V202606140001")
            @RequestParam String voucherCode) {
        return Result.success(verifyService.listByVoucherCode(voucherCode));
    }

    @Operation(summary = "园区当日检票成功统计")
    @GetMapping("/today-stats")
    public Result<Map<String, Object>> todayStats(
            @Parameter(description = "园区 ID", example = "1") @RequestParam Long scenicId) {
        return Result.success(Map.of(
                "scenicId", scenicId,
                "date", java.time.LocalDate.now().toString(),
                "successCount", verifyService.countTodaySuccess(scenicId)));
    }
}

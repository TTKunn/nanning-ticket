package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.SettlementActionDTO;
import com.ainanning.ticketing.dto.SettlementCreateDTO;
import com.ainanning.ticketing.dto.SettlementQueryDTO;
import com.ainanning.ticketing.service.ChannelSettlementService;
import com.ainanning.ticketing.vo.ChannelSettlementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 渠道结算单 Controller
 *
 * <p>路由前缀：/api/channel-settlements
 * <br>专用于渠道对账 / 结算 / 打款全流程。
 * 状态机：{@code 待确认 → 已确认 → 已打款}，任一状态可作废。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "09. 渠道管理-结算单", description = "渠道对账 / 结算 / 打款")
@Validated
@RestController
@RequestMapping("/api/channel-settlements")
@RequiredArgsConstructor
public class ChannelSettlementController {

    private final ChannelSettlementService settlementService;

    @Operation(summary = "分页查询结算单",
            description = "支持渠道 / 状态 / 周期 / 关键字 多维过滤")
    @GetMapping
    public Result<PageVO<ChannelSettlementVO>> page(@Valid SettlementQueryDTO query) {
        return Result.success(settlementService.page(query));
    }

    @Operation(summary = "结算单详情")
    @GetMapping("/{id}")
    public Result<ChannelSettlementVO> getById(
            @Parameter(description = "结算单 ID") @PathVariable Long id) {
        return Result.success(settlementService.getById(id));
    }

    @Operation(summary = "按结算单号查询")
    @GetMapping("/by-no")
    public Result<ChannelSettlementVO> getByNo(
            @Parameter(description = "结算单号", example = "CS202606001")
            @RequestParam String settlementNo) {
        return Result.success(settlementService.getByNo(settlementNo));
    }

    @Operation(summary = "生成结算单（指定渠道 + 周期，从 order 表聚合）")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody SettlementCreateDTO dto) {
        return Result.success(settlementService.createSettlement(dto));
    }

    @Operation(summary = "确认结算单（待确认 → 已确认）")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(
            @Parameter(description = "结算单 ID") @PathVariable Long id,
            @RequestBody(required = false) SettlementActionDTO dto) {
        settlementService.confirm(id, dto);
        return Result.success();
    }

    @Operation(summary = "打款（已确认 → 已打款，paidAmount 默认按 payableAmount）")
    @PostMapping("/{id}/pay")
    public Result<Void> pay(
            @Parameter(description = "结算单 ID") @PathVariable Long id,
            @RequestBody(required = false) SettlementActionDTO dto) {
        settlementService.pay(id, dto);
        return Result.success();
    }

    @Operation(summary = "作废结算单（已打款不能作废）")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(
            @Parameter(description = "结算单 ID") @PathVariable Long id,
            @RequestBody(required = false) SettlementActionDTO dto) {
        settlementService.cancel(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除结算单（仅\"已作废\"状态）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(
            @Parameter(description = "结算单 ID") @PathVariable Long id) {
        settlementService.deleteById(id);
        return Result.success();
    }
}

package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.ChannelCommissionDTO;
import com.ainanning.ticketing.dto.ChannelQueryDTO;
import com.ainanning.ticketing.dto.ChannelSaveDTO;
import com.ainanning.ticketing.dto.ChannelStatusDTO;
import com.ainanning.ticketing.service.ChannelService;
import com.ainanning.ticketing.vo.ChannelOptionVO;
import com.ainanning.ticketing.vo.ChannelStatsVO;
import com.ainanning.ticketing.vo.ChannelVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 渠道 Controller
 *
 * <p>路由前缀：/api/channels
 * <br>本 Controller 只管"主数据 CRUD + 状态/佣金调整"，
 * 结算单由 {@code ChannelSettlementController} 单独处理。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "09. 渠道管理", description = "在线分销渠道主数据 / 状态 / 佣金 / 统计")
@Validated
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @Operation(summary = "分页查询渠道",
            description = "支持关键字 / 类型 / 状态 多维过滤")
    @GetMapping
    public Result<PageVO<ChannelVO>> page(@Valid ChannelQueryDTO query) {
        return Result.success(channelService.page(query));
    }

    @Operation(summary = "渠道详情")
    @GetMapping("/{id}")
    public Result<ChannelVO> getById(
            @Parameter(description = "渠道 ID") @PathVariable Long id) {
        return Result.success(channelService.getById(id));
    }

    @Operation(summary = "按编码查询")
    @GetMapping("/by-code")
    public Result<ChannelVO> getByCode(
            @Parameter(description = "渠道编码", example = "OTA")
            @RequestParam String channelCode) {
        return Result.success(channelService.getByCode(channelCode));
    }

    @Operation(summary = "新增 / 修改渠道（id 为空 = 新增）")
    @PostMapping
    public Result<Long> save(@Valid @RequestBody ChannelSaveDTO dto) {
        return Result.success(channelService.save(dto));
    }

    @Operation(summary = "删除渠道（有关联订单时拒绝）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(
            @Parameter(description = "渠道 ID") @PathVariable Long id) {
        channelService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "切换渠道状态")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "渠道 ID") @PathVariable Long id,
            @Valid @RequestBody ChannelStatusDTO dto) {
        channelService.updateStatus(id, dto);
        return Result.success();
    }

    @Operation(summary = "调整佣金比例（历史结算单不受影响——已快照）")
    @PatchMapping("/{id}/commission")
    public Result<Void> updateCommission(
            @Parameter(description = "渠道 ID") @PathVariable Long id,
            @Valid @RequestBody ChannelCommissionDTO dto) {
        channelService.updateCommission(id, dto);
        return Result.success();
    }

    @Operation(summary = "渠道下拉选项（仅启用项，按 sort 倒序）")
    @GetMapping("/options")
    public Result<List<ChannelOptionVO>> listOptions() {
        return Result.success(channelService.listOptions());
    }

    @Operation(summary = "渠道维度统计",
            description = "返回总数 / 启用数 / 停用数 / 各渠道汇总行（按 GMV 倒序）")
    @GetMapping("/stats")
    public Result<ChannelStatsVO> stats() {
        return Result.success(channelService.stats());
    }
}

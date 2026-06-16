package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.SettingQueryDTO;
import com.ainanning.ticketing.dto.SettingSaveDTO;
import com.ainanning.ticketing.service.SettingService;
import com.ainanning.ticketing.vo.SettingVO;
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
 * 系统参数 Controller
 *
 * <p>路由前缀：/api/settings
 * <br>本 Controller 管 system-wide 配置。{@code isReadonly=1} 的参数仅展示，不允许修改。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "11. 系统设置-参数", description = "系统参数 K-V 配置（订单超时、默认支付方式等）")
@Validated
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @Operation(summary = "分页查询参数")
    @GetMapping
    public Result<PageVO<SettingVO>> page(@Valid SettingQueryDTO query) {
        return Result.success(settingService.page(query));
    }

    @Operation(summary = "参数详情")
    @GetMapping("/{id}")
    public Result<SettingVO> getById(
            @Parameter(description = "参数 ID") @PathVariable Long id) {
        return Result.success(settingService.getById(id));
    }

    @Operation(summary = "按 key 查")
    @GetMapping("/by-key")
    public Result<SettingVO> getByKey(
            @Parameter(description = "参数键", example = "ORDER_TIMEOUT_MIN")
            @RequestParam String key) {
        return Result.success(settingService.getByKey(key));
    }

    @Operation(summary = "按分组批量取（仅启用项）")
    @GetMapping("/by-group")
    public Result<List<SettingVO>> listByGroup(
            @Parameter(description = "分组", example = "订单")
            @RequestParam String group) {
        return Result.success(settingService.listByGroup(group));
    }

    @Operation(summary = "新增 / 修改参数",
            description = "id 为空 = 新增；非空 = 修改（只读参数拒绝修改）")
    @PostMapping
    public Result<Long> save(@Valid @RequestBody SettingSaveDTO dto) {
        return Result.success(settingService.save(dto));
    }

    @Operation(summary = "切换状态（启用/停用）")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "参数 ID") @PathVariable Long id,
            @Parameter(description = "状态", example = "启用")
            @RequestParam String status) {
        settingService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除参数（仅非只读）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(
            @Parameter(description = "参数 ID") @PathVariable Long id) {
        settingService.deleteById(id);
        return Result.success();
    }
}

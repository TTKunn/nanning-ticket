package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.RuleQueryDTO;
import com.ainanning.ticketing.dto.RuleSaveDTO;
import com.ainanning.ticketing.service.RuleService;
import com.ainanning.ticketing.vo.RuleOptionVO;
import com.ainanning.ticketing.vo.RuleVO;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目规则 Controller
 *
 * <p>路由前缀：/api/rules
 * <br>规则隶属于某个园区，因此列表/选项等接口均需传入 scenicId。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "02. 项目规则配置", description = "园区下的折扣/免票/团体/时段/限流等业务规则")
@Validated
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @Operation(summary = "分页查询规则（按园区）")
    @GetMapping
    public Result<PageVO<RuleVO>> page(@Valid RuleQueryDTO query) {
        return Result.success(ruleService.page(query));
    }

    @Operation(summary = "获取规则详情")
    @GetMapping("/{id}")
    public Result<RuleVO> getById(
            @Parameter(description = "规则 ID") @PathVariable Long id) {
        return Result.success(ruleService.getById(id));
    }

    @Operation(summary = "新建规则")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody RuleSaveDTO dto) {
        Long id = ruleService.create(dto);
        return Result.success("新建成功", id);
    }

    @Operation(summary = "更新规则")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "规则 ID") @PathVariable Long id,
            @Valid @RequestBody RuleSaveDTO dto) {
        dto.setId(id);
        ruleService.update(dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "启用 / 禁用规则")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "规则 ID") @PathVariable Long id,
            @Parameter(description = "状态：启用 / 禁用") @RequestParam String status) {
        ruleService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }

    @Operation(summary = "删除规则（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "规则 ID") @PathVariable Long id) {
        ruleService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取园区下启用的规则下拉选项")
    @GetMapping("/options")
    public Result<List<RuleOptionVO>> listOptions(
            @Parameter(description = "园区 ID", required = true)
            @RequestParam Long scenicId) {
        return Result.success(ruleService.listOptions(scenicId));
    }
}

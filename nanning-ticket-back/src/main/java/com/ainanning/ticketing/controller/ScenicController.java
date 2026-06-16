package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.ScenicQueryDTO;
import com.ainanning.ticketing.dto.ScenicSaveDTO;
import com.ainanning.ticketing.service.ScenicService;
import com.ainanning.ticketing.vo.ScenicOptionVO;
import com.ainanning.ticketing.vo.ScenicVO;
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
 * 园区管理 Controller
 *
 * <p>路由前缀：/api/scenics</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "01. 园区管理", description = "园区基础信息、收费项目与规则入口")
@Validated
@RestController
@RequestMapping("/api/scenics")
@RequiredArgsConstructor
public class ScenicController {

    private final ScenicService scenicService;

    @Operation(summary = "分页查询园区")
    @GetMapping
    public Result<PageVO<ScenicVO>> page(@Valid ScenicQueryDTO query) {
        return Result.success(scenicService.page(query));
    }

    @Operation(summary = "获取园区详情")
    @GetMapping("/{id}")
    public Result<ScenicVO> getById(
            @Parameter(description = "园区 ID") @PathVariable Long id) {
        return Result.success(scenicService.getById(id));
    }

    @Operation(summary = "新建园区")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ScenicSaveDTO dto) {
        Long id = scenicService.create(dto);
        return Result.success("新建成功", id);
    }

    @Operation(summary = "更新园区")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "园区 ID") @PathVariable Long id,
            @Valid @RequestBody ScenicSaveDTO dto) {
        dto.setId(id);
        scenicService.update(dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "启用 / 暂停园区")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "园区 ID") @PathVariable Long id,
            @Parameter(description = "状态：运营中 / 暂停运营") @RequestParam String status) {
        scenicService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }

    @Operation(summary = "删除园区（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "园区 ID") @PathVariable Long id) {
        scenicService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取园区下拉选项")
    @GetMapping("/options")
    public Result<List<ScenicOptionVO>> listOptions() {
        return Result.success(scenicService.listOptions());
    }
}

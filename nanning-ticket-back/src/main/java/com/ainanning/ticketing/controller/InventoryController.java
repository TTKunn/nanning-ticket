package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.InventoryBatchDTO;
import com.ainanning.ticketing.dto.InventoryQueryDTO;
import com.ainanning.ticketing.dto.InventorySaveDTO;
import com.ainanning.ticketing.service.InventoryService;
import com.ainanning.ticketing.vo.InventoryVO;
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

/**
 * 库存 Controller
 *
 * <p>路由前缀：/api/inventories
 * <br>按"票种 × 日期"粒度管理每日库存，支持单日创建与日期范围批量创建。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "04. 库存管理", description = "按票种与日期维度的库存 CRUD，含批量按区间创建")
@Validated
@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "分页查询库存")
    @GetMapping
    public Result<PageVO<InventoryVO>> page(@Valid InventoryQueryDTO query) {
        return Result.success(inventoryService.page(query));
    }

    @Operation(summary = "获取库存详情")
    @GetMapping("/{id}")
    public Result<InventoryVO> getById(
            @Parameter(description = "库存 ID") @PathVariable Long id) {
        return Result.success(inventoryService.getById(id));
    }

    @Operation(summary = "新建单日库存")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody InventorySaveDTO dto) {
        Long id = inventoryService.create(dto);
        return Result.success("新建成功", id);
    }

    @Operation(summary = "按日期范围批量创建库存")
    @PostMapping("/batch")
    public Result<Integer> createBatch(@Valid @RequestBody InventoryBatchDTO dto) {
        int count = inventoryService.createBatch(dto);
        return Result.success("批量创建完成", count);
    }

    @Operation(summary = "更新库存（总库存 / 状态 / 备注）")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "库存 ID") @PathVariable Long id,
            @Valid @RequestBody InventorySaveDTO dto) {
        dto.setId(id);
        inventoryService.update(dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "开放 / 关闭 库存")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "库存 ID") @PathVariable Long id,
            @Parameter(description = "状态：开放 / 关闭 / 售罄") @RequestParam String status) {
        inventoryService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }

    @Operation(summary = "删除库存（软删除；已售记录的库存不允许删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "库存 ID") @PathVariable Long id) {
        inventoryService.deleteById(id);
        return Result.success("删除成功", null);
    }
}

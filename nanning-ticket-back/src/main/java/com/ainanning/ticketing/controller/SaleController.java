package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.SaleCreateDTO;
import com.ainanning.ticketing.dto.SaleQueryDTO;
import com.ainanning.ticketing.dto.SaleRefundDTO;
import com.ainanning.ticketing.service.SaleService;
import com.ainanning.ticketing.vo.SaleVO;
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
 * 窗口售票 Controller
 *
 * <p>路由前缀：/api/sales
 * <br>覆盖窗口售票流程：创建销售、退票（整单/部分）、取消、查询。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "05. 窗口售票", description = "窗口出票：创建销售、退票、取消、查询")
@Validated
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @Operation(summary = "分页查询销售单")
    @GetMapping
    public Result<PageVO<SaleVO>> page(@Valid SaleQueryDTO query) {
        return Result.success(saleService.page(query));
    }

    @Operation(summary = "获取销售单详情（含明细）")
    @GetMapping("/{id}")
    public Result<SaleVO> getById(
            @Parameter(description = "销售单 ID") @PathVariable Long id) {
        return Result.success(saleService.getById(id));
    }

    @Operation(summary = "窗口售票（创建销售）",
            description = "一次窗口出票可包含多个明细（同/不同票种，不同入场日期）")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody SaleCreateDTO dto) {
        Long id = saleService.create(dto);
        return Result.success("售票成功", id);
    }

    @Operation(summary = "退票",
            description = "items 为空/null → 整单退；否则按明细部分退")
    @PostMapping("/{id}/refund")
    public Result<Void> refund(
            @Parameter(description = "销售单 ID") @PathVariable Long id,
            @Valid @RequestBody SaleRefundDTO dto) {
        saleService.refund(id, dto);
        return Result.success("退票成功", null);
    }

    @Operation(summary = "取消销售单",
            description = "仅允许取消已支付订单（系统将全单退票并恢复库存）")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(
            @Parameter(description = "销售单 ID") @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam(required = false) String reason) {
        saleService.cancel(id, reason);
        return Result.success("取消成功", null);
    }

    @Operation(summary = "删除销售记录",
            description = "仅允许删除已取消订单；其余状态需先取消")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "销售单 ID") @PathVariable Long id) {
        saleService.deleteById(id);
        return Result.success("删除成功", null);
    }
}

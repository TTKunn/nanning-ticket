package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.TicketQueryDTO;
import com.ainanning.ticketing.dto.TicketSaveDTO;
import com.ainanning.ticketing.service.TicketService;
import com.ainanning.ticketing.vo.TicketOptionVO;
import com.ainanning.ticketing.vo.TicketVO;
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
 * 票种 Controller
 *
 * <p>路由前缀：/api/tickets
 * <br>票种隶属于某个园区，列表/选项等接口均需传入 scenicId。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "03. 票种管理", description = "园区下的单票/套票/联票等可售商品")
@Validated
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "分页查询票种（按园区）")
    @GetMapping
    public Result<PageVO<TicketVO>> page(@Valid TicketQueryDTO query) {
        return Result.success(ticketService.page(query));
    }

    @Operation(summary = "获取票种详情")
    @GetMapping("/{id}")
    public Result<TicketVO> getById(
            @Parameter(description = "票种 ID") @PathVariable Long id) {
        return Result.success(ticketService.getById(id));
    }

    @Operation(summary = "新建票种")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TicketSaveDTO dto) {
        Long id = ticketService.create(dto);
        return Result.success("新建成功", id);
    }

    @Operation(summary = "更新票种")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "票种 ID") @PathVariable Long id,
            @Valid @RequestBody TicketSaveDTO dto) {
        dto.setId(id);
        ticketService.update(dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "上架 / 下架票种")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "票种 ID") @PathVariable Long id,
            @Parameter(description = "状态：在售 / 停售") @RequestParam String status) {
        ticketService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }

    @Operation(summary = "删除票种（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "票种 ID") @PathVariable Long id) {
        ticketService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取园区下在售的票种下拉选项")
    @GetMapping("/options")
    public Result<List<TicketOptionVO>> listOptions(
            @Parameter(description = "园区 ID", required = true)
            @RequestParam Long scenicId) {
        return Result.success(ticketService.listOptions(scenicId));
    }
}

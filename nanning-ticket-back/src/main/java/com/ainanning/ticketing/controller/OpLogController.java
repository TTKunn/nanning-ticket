package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.OpLogQueryDTO;
import com.ainanning.ticketing.service.OpLogService;
import com.ainanning.ticketing.vo.OpLogVO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 操作日志 Controller
 *
 * <p>路由前缀：/api/op-logs
 * <br>本 Controller 仅供"管理后台查看"使用；写入由业务 Service 显式调用
 * {@code OpLogService.record()} 完成，不对外暴露 record 接口以防被滥用。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "11. 系统设置-操作日志", description = "管理后台审计追踪（管理 / 财务 / 运营 角色）")
@Validated
@RestController
@RequestMapping("/api/op-logs")
@RequiredArgsConstructor
public class OpLogController {

    private final OpLogService opLogService;

    @Operation(summary = "分页查询操作日志",
            description = "支持模块 / 动作 / 状态 / 操作人 / 业务 / 时间范围 多维过滤")
    @GetMapping
    public Result<PageVO<OpLogVO>> page(@Valid OpLogQueryDTO query) {
        return Result.success(opLogService.page(query));
    }

    @Operation(summary = "日志详情")
    @GetMapping("/{id}")
    public Result<OpLogVO> getById(
            @Parameter(description = "日志 ID") @PathVariable Long id) {
        return Result.success(opLogService.getById(id));
    }

    @Operation(summary = "删除单条日志",
            description = "审计要求保留日志——仅允许手动清理（不走批量删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(
            @Parameter(description = "日志 ID") @PathVariable Long id) {
        opLogService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "清理 N 天前的日志（运维专用）",
            description = "调用方一般是定时任务；retentionDays 不传则按系统参数 SYS_OP_LOG_RETENTION")
    @PostMapping("/clean")
    public Result<Integer> cleanBefore(
            @Parameter(description = "保留天数（1-3650），不传则按系统参数")
            @RequestParam(required = false) Integer retentionDays) {
        int days = (retentionDays == null) ? 180 : retentionDays;
        return Result.success(opLogService.cleanBefore(days));
    }
}

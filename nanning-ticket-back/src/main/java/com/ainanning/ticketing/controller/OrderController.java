package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.OrderCancelDTO;
import com.ainanning.ticketing.dto.OrderCreateDTO;
import com.ainanning.ticketing.dto.OrderPayDTO;
import com.ainanning.ticketing.dto.OrderQueryDTO;
import com.ainanning.ticketing.dto.OrderRefundDTO;
import com.ainanning.ticketing.service.OrderService;
import com.ainanning.ticketing.vo.OrderStatsVO;
import com.ainanning.ticketing.vo.OrderVO;
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

import java.util.List;

/**
 * 在线订单 Controller
 *
 * <p>路由前缀：/api/orders
 * <br>本原型创建订单 = 创建+支付+出票（"一气呵成"模式），如需拆成异步两步后续再扩展。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "08. 订单管理", description = "在线渠道下单 / 支付 / 取消 / 退款 / 统计")
@Validated
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单",
            description = "支持园区/渠道/状态/支付方式/手机/关键字/下单日期/入场日期 多维过滤")
    @GetMapping
    public Result<PageVO<OrderVO>> page(@Valid OrderQueryDTO query) {
        return Result.success(orderService.page(query));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> getById(
            @Parameter(description = "订单 ID") @PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @Operation(summary = "创建订单（创建即支付）",
            description = "本原型一气呵成：下单→模拟支付→出票。返回订单 ID。")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.create(dto));
    }

    @Operation(summary = "支付（仅\"待支付\"可调）")
    @PostMapping("/{id}/pay")
    public Result<Void> pay(
            @Parameter(description = "订单 ID") @PathVariable Long id,
            @Valid @RequestBody OrderPayDTO dto) {
        orderService.pay(id, dto);
        return Result.success();
    }

    @Operation(summary = "取消订单（仅\"待支付\"可调，已出票走 refund）")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(
            @Parameter(description = "订单 ID") @PathVariable Long id,
            @RequestBody(required = false) OrderCancelDTO dto) {
        orderService.cancel(id, dto == null ? new OrderCancelDTO() : dto);
        return Result.success();
    }

    @Operation(summary = "全单退款（仅\"已出票\"可调，voucher 必须全部未使用）")
    @PostMapping("/{id}/refund")
    public Result<Void> refund(
            @Parameter(description = "订单 ID") @PathVariable Long id,
            @RequestBody(required = false) OrderRefundDTO dto) {
        orderService.refund(id, dto == null ? new OrderRefundDTO() : dto);
        return Result.success();
    }

    @Operation(summary = "删除订单（仅\"已取消\"或\"已退款\"状态）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(
            @Parameter(description = "订单 ID") @PathVariable Long id) {
        orderService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "按渠道列订单（不分页）")
    @GetMapping("/by-channel")
    public Result<List<OrderVO>> listByChannelCode(
            @Parameter(description = "渠道编码", example = "OTA")
            @RequestParam String channelCode) {
        return Result.success(orderService.listByChannelCode(channelCode));
    }

    @Operation(summary = "订单状态统计",
            description = "按园区 / 渠道两维度过滤统计；6 状态计数 + GMV + 出票率")
    @GetMapping("/stats")
    public Result<OrderStatsVO> stats(
            @Parameter(description = "园区 ID（可空）") @RequestParam(required = false) Long scenicId,
            @Parameter(description = "渠道编码（可空）") @RequestParam(required = false) String channelCode) {
        return Result.success(orderService.stats(scenicId, channelCode));
    }
}

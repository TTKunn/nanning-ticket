package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.dto.ReportQueryDTO;
import com.ainanning.ticketing.service.ReportService;
import com.ainanning.ticketing.vo.ReportInventoryVO;
import com.ainanning.ticketing.vo.ReportOverviewVO;
import com.ainanning.ticketing.vo.ReportPaymentVO;
import com.ainanning.ticketing.vo.ReportRankingVO;
import com.ainanning.ticketing.vo.ReportTrendVO;
import com.ainanning.ticketing.vo.ReportVisitFunnelVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据报表 Controller
 *
 * <p>路由前缀：/api/reports
 * <br>所有端点都是 GET，请求体走 {@code @ModelAttribute}（即 URL Query 参数），
 * 适配前端 "把筛选条件塞 URL" 的常见模式。</p>
 *
 * <p>本模块 = 纯读聚合层。{@code dateFrom} / {@code dateTo} 必填且间隔 ≤ 366 天。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "10. 数据报表", description = "运营总览 / 时间趋势 / 排名 / 检票漏斗 / 库存日报 / 支付分布")
@Validated
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "顶部核心指标卡",
            description = "GMV / 退款 / 净收入 / 核销率 / 库存售出率等 16 个数字")
    @GetMapping("/overview")
    public Result<ReportOverviewVO> overview(@Valid @ModelAttribute ReportQueryDTO query) {
        return Result.success(reportService.overview(query));
    }

    @Operation(summary = "时间趋势（折线 / 柱状）",
            description = "interval = DAY / WEEK / MONTH（默认 DAY）")
    @GetMapping("/trend")
    public Result<ReportTrendVO> trend(@Valid @ModelAttribute ReportQueryDTO query) {
        return Result.success(reportService.trend(query));
    }

    @Operation(summary = "多维排名",
            description = "groupBy = CHANNEL / SCENIC / TICKET / PAY_METHOD / WINDOW（默认 CHANNEL）")
    @GetMapping("/ranking")
    public Result<ReportRankingVO> ranking(@Valid @ModelAttribute ReportQueryDTO query) {
        return Result.success(reportService.ranking(query));
    }

    @Operation(summary = "检票转化漏斗（订单 → 出票 → 核销）")
    @GetMapping("/visit-funnel")
    public Result<ReportVisitFunnelVO> visitFunnel(@Valid @ModelAttribute ReportQueryDTO query) {
        return Result.success(reportService.visitFunnel(query));
    }

    @Operation(summary = "库存与售票日报（按日期 × 票种展开）")
    @GetMapping("/inventory")
    public Result<ReportInventoryVO> inventory(@Valid @ModelAttribute ReportQueryDTO query) {
        return Result.success(reportService.inventory(query));
    }

    @Operation(summary = "支付方式分布（现金 / 微信 / 支付宝 / 银行卡 / 余额）")
    @GetMapping("/payment")
    public Result<ReportPaymentVO> payment(@Valid @ModelAttribute ReportQueryDTO query) {
        return Result.success(reportService.payment(query));
    }
}

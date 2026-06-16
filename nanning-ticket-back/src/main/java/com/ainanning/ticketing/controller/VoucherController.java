package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.VoucherMarkPrintedDTO;
import com.ainanning.ticketing.dto.VoucherQueryDTO;
import com.ainanning.ticketing.dto.VoucherReissueDTO;
import com.ainanning.ticketing.dto.VoucherRevokeDTO;
import com.ainanning.ticketing.service.VoucherService;
import com.ainanning.ticketing.vo.VoucherStatsVO;
import com.ainanning.ticketing.vo.VoucherVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 票据 Controller
 *
 * <p>路由前缀：/api/vouchers
 * <br>票据 = 销售出票后的最小可核销单元，每张票一个独立实体。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "07. 票据管理", description = "票据查询 / 作废 / 补发 / 打印 / 统计")
@Validated
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "分页查询票据")
    @GetMapping
    public Result<PageVO<VoucherVO>> page(@Valid VoucherQueryDTO query) {
        return Result.success(voucherService.page(query));
    }

    @Operation(summary = "获取票据详情")
    @GetMapping("/{id}")
    public Result<VoucherVO> getById(
            @Parameter(description = "票据 ID") @PathVariable Long id) {
        return Result.success(voucherService.getById(id));
    }

    @Operation(summary = "按票据码查询（扫码/手输反查）")
    @GetMapping("/by-code")
    public Result<VoucherVO> getByCode(
            @Parameter(description = "票据码", example = "V202606140001")
            @RequestParam String voucherCode) {
        return Result.success(voucherService.getByCode(voucherCode));
    }

    @Operation(summary = "按销售明细 ID 查明细的全部票据")
    @GetMapping("/by-sale-item")
    public Result<List<VoucherVO>> listBySaleItemId(
            @Parameter(description = "销售明细 ID") @RequestParam Long saleItemId) {
        return Result.success(voucherService.listBySaleItemId(saleItemId));
    }

    @Operation(summary = "按销售单 ID 查该单的全部票据")
    @GetMapping("/by-sale")
    public Result<List<VoucherVO>> listBySaleId(
            @Parameter(description = "销售单 ID") @RequestParam Long saleId) {
        return Result.success(voucherService.listBySaleId(saleId));
    }

    @Operation(summary = "批量作废（管理端，区别于退票）",
            description = "作废是管理行为（损坏/错发），退票是销售流水回滚。已使用/已退的票不能作废。")
    @PostMapping("/revoke")
    public Result<Integer> revoke(@Valid @RequestBody VoucherRevokeDTO dto) {
        return Result.success(voucherService.revoke(dto));
    }

    @Operation(summary = "批量补发",
            description = "针对已退/已作废的票生成新码（待使用），原票保留做审计。")
    @PostMapping("/reissue")
    public Result<List<VoucherVO>> reissue(@Valid @RequestBody VoucherReissueDTO dto) {
        return Result.success(voucherService.reissue(dto));
    }

    @Operation(summary = "标记打印（重打时自增 printCount）",
            description = "每次重打纸质凭据时调用，审计打印次数。")
    @PostMapping("/mark-printed")
    public Result<Integer> markPrinted(@Valid @RequestBody VoucherMarkPrintedDTO dto) {
        return Result.success(voucherService.markPrinted(dto.getIds()));
    }

    @Operation(summary = "状态统计",
            description = "按园区 / 票种 / 销售单三维度过滤统计。三个参数均可空，全空为全表统计。")
    @GetMapping("/stats")
    public Result<VoucherStatsVO> stats(
            @Parameter(description = "园区 ID（可空）") @RequestParam(required = false) Long scenicId,
            @Parameter(description = "票种 ID（可空）") @RequestParam(required = false) Long ticketId,
            @Parameter(description = "销售单 ID（可空）") @RequestParam(required = false) Long saleId) {
        return Result.success(voucherService.stats(scenicId, ticketId, saleId));
    }
}

package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.VoucherQueryDTO;
import com.ainanning.ticketing.dto.VoucherReissueDTO;
import com.ainanning.ticketing.dto.VoucherRevokeDTO;
import com.ainanning.ticketing.entity.Order;
import com.ainanning.ticketing.entity.OrderItem;
import com.ainanning.ticketing.entity.Sale;
import com.ainanning.ticketing.entity.SaleItem;
import com.ainanning.ticketing.entity.Ticket;
import com.ainanning.ticketing.vo.VoucherStatsVO;
import com.ainanning.ticketing.vo.VoucherVO;

import java.util.List;

/**
 * 票据业务接口
 *
 * <p>票据 = 销售出票后的最小可核销单元。每售出 1 张就生成 1 条 voucher 记录。</p>
 *
 * @author nanning-ticket
 */
public interface VoucherService {

    /** 分页查询票据 */
    PageVO<VoucherVO> page(VoucherQueryDTO query);

    /** 票据详情 */
    VoucherVO getById(Long id);

    /** 按票据码查询 */
    VoucherVO getByCode(String voucherCode);

    /** 按销售明细 ID 查明细的所有票据 */
    List<VoucherVO> listBySaleItemId(Long saleItemId);

    /** 按销售单 ID 查该单的所有票据 */
    List<VoucherVO> listBySaleId(Long saleId);

    /** 批量作废 */
    int revoke(VoucherRevokeDTO dto);

    /** 批量补发（针对已退 / 已作废的票生成新码） */
    List<VoucherVO> reissue(VoucherReissueDTO dto);

    /** 标记打印（重打时自增 printCount） */
    int markPrinted(List<Long> ids);

    /** 状态统计（园区 / 票种 / 销售单三维度） */
    VoucherStatsVO stats(Long scenicId, Long ticketId, Long saleId);

    /**
     * 出票：在销售单创建时调用，按 quantity 循环生成 voucher 记录
     *
     * <p>供 {@code SaleServiceImpl.create} 调用，集中管理票据码生成与有效期计算。</p>
     *
     * @return 该明细下新生成的 voucher_code 列表（同时回写 sale_item.voucher_codes）
     */
    List<String> issue(Sale sale, SaleItem item, Ticket ticket, int quantity);

    /**
     * 出票：在线订单的对应版本（{@code OrderItem} vs {@code SaleItem} 类型不同，但内部逻辑一致）
     *
     * <p>供 {@code OrderServiceImpl.create / pay} 调用。</p>
     *
     * @return 该明细下新生成的 voucher_code 列表（同时回写 order_item.voucher_codes）
     */
    List<String> issueForOrder(Order order, OrderItem item, Ticket ticket, int quantity);

    /**
     * 部分退票：把指定 sale_item 下指定数量的 voucher 标记为"已退"
     *
     * <p>调用方需保证 quantity &lt;= (未退 - 已退)；Service 内只按 ID 顺序回退前 N 张未使用票。</p>
     *
     * @return 实际标记为已退的 voucher 数量
     */
    int markRefunded(Long saleItemId, int quantity);

    /**
     * 核销：把指定 voucher 标记为"已使用"
     */
    boolean markUsed(String voucherCode, Long staffId, String staffName,
                     Long deviceId, String deviceName);

    /**
     * 全单退票：把指定 sale 单下所有未使用 voucher 标记为"已退"
     */
    int markAllRefundedBySaleId(Long saleId);
}

package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.SaleCreateDTO;
import com.ainanning.ticketing.dto.SaleQueryDTO;
import com.ainanning.ticketing.dto.SaleRefundDTO;
import com.ainanning.ticketing.vo.SaleVO;

/**
 * 窗口售票业务接口
 *
 * @author nanning-ticket
 */
public interface SaleService {

    /** 分页查询销售单 */
    PageVO<SaleVO> page(SaleQueryDTO query);

    /** 获取销售单详情（含明细） */
    SaleVO getById(Long id);

    /**
     * 创建销售（售票）
     *
     * <p>内部完成：生成 saleNo、校验票种/库存/支付方式、扣减库存、生成临时票据码。</p>
     *
     * @return 新建销售单 ID
     */
    Long create(SaleCreateDTO dto);

    /**
     * 退票
     *
     * <p>支持整单退（{@code dto.items} 为空）和部分退；恢复相应库存、更新明细与主单状态。</p>
     */
    void refund(Long id, SaleRefundDTO dto);

    /**
     * 取消销售单
     *
     * <p>仅允许"已支付"状态取消；取消即视为全单退票。</p>
     */
    void cancel(Long id, String reason);

    /**
     * 删除销售记录（仅允许"已取消"状态，且无任何核销记录）
     */
    void deleteById(Long id);
}

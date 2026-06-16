package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.TicketQueryDTO;
import com.ainanning.ticketing.dto.TicketSaveDTO;
import com.ainanning.ticketing.vo.TicketOptionVO;
import com.ainanning.ticketing.vo.TicketVO;

import java.util.List;

/**
 * 票种业务接口
 *
 * @author nanning-ticket
 */
public interface TicketService {

    /** 分页查询票种（按园区过滤） */
    PageVO<TicketVO> page(TicketQueryDTO query);

    /** 获取票种详情 */
    TicketVO getById(Long id);

    /** 新建票种 */
    Long create(TicketSaveDTO dto);

    /** 更新票种 */
    void update(TicketSaveDTO dto);

    /** 上架 / 下架票种 */
    void updateStatus(Long id, String status);

    /** 删除票种（软删除） */
    void deleteById(Long id);

    /**
     * 获取某园区下在售的票种下拉选项
     *
     * @param scenicId 园区 ID（为 null 时返回空列表）
     * @return 票种选项列表
     */
    List<TicketOptionVO> listOptions(Long scenicId);
}

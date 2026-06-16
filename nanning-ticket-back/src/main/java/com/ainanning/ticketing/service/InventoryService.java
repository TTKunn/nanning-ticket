package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.InventoryBatchDTO;
import com.ainanning.ticketing.dto.InventoryQueryDTO;
import com.ainanning.ticketing.dto.InventorySaveDTO;
import com.ainanning.ticketing.vo.InventoryVO;

/**
 * 库存业务接口
 *
 * @author nanning-ticket
 */
public interface InventoryService {

    /** 分页查询库存 */
    PageVO<InventoryVO> page(InventoryQueryDTO query);

    /** 获取库存详情 */
    InventoryVO getById(Long id);

    /** 新建单日库存 */
    Long create(InventorySaveDTO dto);

    /**
     * 批量按日期范围创建库存
     *
     * @return 实际新建的记录数（跳过已存在的）
     */
    int createBatch(InventoryBatchDTO dto);

    /** 更新库存（总库存 / 状态 / 备注） */
    void update(InventorySaveDTO dto);

    /** 开放 / 关闭 库存 */
    void updateStatus(Long id, String status);

    /** 删除库存（软删除；已售记录的库存不允许删除） */
    void deleteById(Long id);
}

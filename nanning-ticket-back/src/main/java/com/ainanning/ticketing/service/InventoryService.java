package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.InventoryBatchDeleteDTO;
import com.ainanning.ticketing.dto.InventoryBatchDTO;
import com.ainanning.ticketing.dto.InventoryBatchUpdateDTO;
import com.ainanning.ticketing.dto.InventoryQueryDTO;
import com.ainanning.ticketing.dto.InventorySaveDTO;
import com.ainanning.ticketing.vo.BatchOpResultVO;
import com.ainanning.ticketing.vo.InventoryVO;

import java.time.LocalDate;
import java.util.List;

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

    /**
     * 批量更新库存（按 ticketId + 日期范围 / 显式日期列表）
     *
     * <p>根据 {@link InventoryBatchUpdateDTO#getOperation()} 决定写入字段。
     * {@code sold>0} 的记录默认跳过，可在 DTO 中关闭该保护。</p>
     */
    BatchOpResultVO updateBatch(InventoryBatchUpdateDTO dto);

    /**
     * 批量删除库存（按 ticketId + 日期范围 / 显式日期列表）
     *
     * <p>默认仅删 {@code sold=0} 的记录；{@code onlyUnsold=false} 强制删除。</p>
     */
    BatchOpResultVO deleteBatch(InventoryBatchDeleteDTO dto);

    /**
     * 列出指定票种已有库存的全部日期（按日期升序）。
     *
     * <p>用于前端"批量创建"弹窗：选定票种后展示已占用的日期，避免重复创建。</p>
     */
    List<LocalDate> listExistingDates(Long ticketId);
}

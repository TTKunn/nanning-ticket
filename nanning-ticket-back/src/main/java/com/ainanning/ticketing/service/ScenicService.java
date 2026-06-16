package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.ScenicQueryDTO;
import com.ainanning.ticketing.dto.ScenicSaveDTO;
import com.ainanning.ticketing.vo.ScenicOptionVO;
import com.ainanning.ticketing.vo.ScenicVO;

import java.util.List;

/**
 * 园区业务接口
 *
 * @author nanning-ticket
 */
public interface ScenicService {

    /** 分页查询园区 */
    PageVO<ScenicVO> page(ScenicQueryDTO query);

    /** 获取园区详情 */
    ScenicVO getById(Long id);

    /** 新建园区 */
    Long create(ScenicSaveDTO dto);

    /** 更新园区 */
    void update(ScenicSaveDTO dto);

    /** 启用 / 暂停园区 */
    void updateStatus(Long id, String status);

    /** 删除园区（软删除） */
    void deleteById(Long id);

    /** 获取园区下拉选项（仅运营中的园区） */
    List<ScenicOptionVO> listOptions();
}

package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.RuleQueryDTO;
import com.ainanning.ticketing.dto.RuleSaveDTO;
import com.ainanning.ticketing.vo.RuleOptionVO;
import com.ainanning.ticketing.vo.RuleVO;

import java.util.List;

/**
 * 项目规则业务接口
 *
 * @author nanning-ticket
 */
public interface RuleService {

    /** 分页查询规则（按园区过滤） */
    PageVO<RuleVO> page(RuleQueryDTO query);

    /** 获取规则详情 */
    RuleVO getById(Long id);

    /** 新建规则 */
    Long create(RuleSaveDTO dto);

    /** 更新规则 */
    void update(RuleSaveDTO dto);

    /** 启用 / 禁用规则 */
    void updateStatus(Long id, String status);

    /** 删除规则（软删除） */
    void deleteById(Long id);

    /**
     * 获取某园区下启用的规则下拉选项
     *
     * @param scenicId 园区 ID
     * @return 规则选项列表
     */
    List<RuleOptionVO> listOptions(Long scenicId);
}

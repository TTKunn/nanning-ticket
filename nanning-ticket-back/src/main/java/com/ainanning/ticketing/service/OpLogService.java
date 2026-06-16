package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.OpLogQueryDTO;
import com.ainanning.ticketing.dto.OpLogRecordDTO;
import com.ainanning.ticketing.vo.OpLogVO;

/**
 * 操作日志业务接口
 *
 * <p>本原型 = 显式记录模式：业务 Service 在"成功" / "失败"出口处调用 {@link #record} 写日志。
 * <br>不接 AOP / Filter，避免引入额外复杂度与失败路径处理。</p>
 *
 * <p>参数截断策略：{@code requestParams} / {@code responseResult} 单字段超过
 * {@code 4000} 字符的会被截断并追加 {@code "..."} 标记。</p>
 *
 * @author nanning-ticket
 */
public interface OpLogService {

    /** 记录一条操作日志（业务侧调用入口） */
    Long record(OpLogRecordDTO dto);

    /** 分页查询 */
    PageVO<OpLogVO> page(OpLogQueryDTO query);

    /** 详情 */
    OpLogVO getById(Long id);

    /** 删除（仅"失败"或超期可清理） */
    void deleteById(Long id);

    /** 清理指定天数前的日志（保留天数不传则按系统参数 SYS_OP_LOG_RETENTION） */
    int cleanBefore(int retentionDays);
}

package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.VerifyQueryDTO;
import com.ainanning.ticketing.dto.VerifyRequestDTO;
import com.ainanning.ticketing.vo.VerifyRecordVO;
import com.ainanning.ticketing.vo.VerifyResultVO;

import java.time.LocalDate;

/**
 * 检票业务接口
 *
 * @author nanning-ticket
 */
public interface VerifyService {

    /**
     * 核心检票接口（闸机/手持终端调用）
     *
     * <p>无论成功失败都返回 {@link VerifyResultVO}，失败原因在 {@code result} + {@code failReason} 字段体现；
     * <br>且无论成功失败都写入 {@code verify_record} 审计表。</p>
     */
    VerifyResultVO verify(VerifyRequestDTO dto);

    /** 分页查询检票记录 */
    PageVO<VerifyRecordVO> page(VerifyQueryDTO query);

    /** 获取检票记录详情 */
    VerifyRecordVO getById(Long id);

    /** 按票据码查询该票的检票历史（成功 + 失败） */
    java.util.List<VerifyRecordVO> listByVoucherCode(String voucherCode);

    /** 园区当日检票统计 */
    long countTodaySuccess(Long scenicId);

    /** 园区指定日期检票统计 */
    long countSuccessByDate(Long scenicId, LocalDate date);
}

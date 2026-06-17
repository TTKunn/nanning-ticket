package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.VerifyRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 检票记录 Mapper
 *
 * @author nanning-ticket
 */
@Mapper
public interface VerifyRecordMapper extends BaseMapper<VerifyRecord> {

    /**
     * 统计某票据码的成功核销次数（用于"是否已被使用"判断）
     */
    @Select("SELECT COUNT(*) FROM verify_record " +
            "WHERE voucher_code = #{voucherCode} " +
            "AND result = '成功' AND deleted_at IS NULL")
    long countSuccessByVoucherCode(String voucherCode);

    /**
     * 统计指定时间窗口内、某园区的检票成功次数
     */
    @Select("SELECT COUNT(*) FROM verify_record " +
            "WHERE scenic_id = #{scenicId} " +
            "AND result = '成功' AND deleted_at IS NULL " +
            "AND verify_time BETWEEN #{from} AND #{to}")
    long countSuccessByScenicAndTime(Long scenicId, java.time.LocalDateTime from,
                                     java.time.LocalDateTime to);

    /**
     * 统计指定时间窗口内全部园区的检票成功次数（跨园区汇总）
     */
    @Select("SELECT COUNT(*) FROM verify_record " +
            "WHERE result = '成功' AND deleted_at IS NULL " +
            "AND verify_time BETWEEN #{from} AND #{to}")
    long countSuccessByTime(java.time.LocalDateTime from, java.time.LocalDateTime to);
}

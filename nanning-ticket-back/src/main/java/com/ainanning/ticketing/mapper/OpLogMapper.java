package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.OpLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author nanning-ticket
 */
@Mapper
public interface OpLogMapper extends BaseMapper<OpLog> {
}

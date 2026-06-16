package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.OpLogQueryDTO;
import com.ainanning.ticketing.dto.OpLogRecordDTO;
import com.ainanning.ticketing.entity.OpLog;
import com.ainanning.ticketing.mapper.OpLogMapper;
import com.ainanning.ticketing.service.OpLogService;
import com.ainanning.ticketing.vo.OpLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@link #record} 由业务 Service 显式调用，不接 AOP</li>
 *   <li>{@code requestParams} / {@code responseResult} 单字段超过 {@value #PARAMS_MAX_LENGTH}
 *       字符会被截断，避免大对象撑爆</li>
 *   <li>{@code status} 默认为"成功"；传 {@code "失败"} 时 {@code errorMsg} 不强制非空（业务可能不填）</li>
 *   <li>{@link #cleanBefore} 清理 N 天前的日志（防止表无限增长）</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpLogServiceImpl implements OpLogService {

    private final OpLogMapper opLogMapper;

    /** 单字段最大长度（超过则截断） */
    private static final int PARAMS_MAX_LENGTH = 4000;

    @Override
    // REQUIRES_NEW：审计日志必须在独立事务中提交，
    // 避免业务事务回滚时连审计日志一起被丢弃
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Long record(OpLogRecordDTO dto) {
        if (dto == null) return null;
        try {
            OpLog entity = new OpLog();
            BeanUtils.copyProperties(dto, entity);

            // 状态兜底
            if (!StringUtils.hasText(entity.getStatus())) {
                entity.setStatus(OpLog.STATUS_SUCCESS);
            }
            // 操作时间兜底
            if (entity.getOpTime() == null) {
                entity.setOpTime(LocalDateTime.now());
            }
            // 参数截断
            entity.setRequestParams(truncate(entity.getRequestParams()));
            entity.setResponseResult(truncate(entity.getResponseResult()));
            entity.setErrorMsg(truncate(entity.getErrorMsg()));

            opLogMapper.insert(entity);
            log.debug("[操作日志] 写入 module={}, action={}, bizNo={}, status={}",
                    entity.getModule(), entity.getAction(), entity.getBizNo(), entity.getStatus());
            return entity.getId();
        } catch (Exception e) {
            // 写日志失败绝不能影响业务主流程
            log.error("[操作日志] 写入失败 dto={}", dto, e);
            return null;
        }
    }

    @Override
    public PageVO<OpLogVO> page(OpLogQueryDTO query) {
        log.info("[操作日志] 分页查询 module={}, action={}, status={}, [{}~{}]",
                query.getModule(), query.getAction(), query.getStatus(),
                query.getOpDateFrom(), query.getOpDateTo());

        Page<OpLog> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<OpLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(OpLog::getDeletedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(OpLog::getBizNo, kw)
                    .or().like(OpLog::getOperatorName, kw)
                    .or().like(OpLog::getRequestUrl, kw));
        }
        if (StringUtils.hasText(query.getModule())) {
            wrapper.eq(OpLog::getModule, query.getModule());
        }
        if (StringUtils.hasText(query.getAction())) {
            wrapper.eq(OpLog::getAction, query.getAction());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(OpLog::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getOperatorName())) {
            wrapper.eq(OpLog::getOperatorName, query.getOperatorName());
        }
        if (query.getBizId() != null) {
            wrapper.eq(OpLog::getBizId, query.getBizId());
        }
        if (query.getOpDateFrom() != null) {
            wrapper.ge(OpLog::getOpTime, query.getOpDateFrom().atStartOfDay());
        }
        if (query.getOpDateTo() != null) {
            wrapper.le(OpLog::getOpTime, query.getOpDateTo().atTime(23, 59, 59));
        }
        wrapper.orderByDesc(OpLog::getOpTime)
                .orderByDesc(OpLog::getId);

        Page<OpLog> result = opLogMapper.selectPage(page, wrapper);
        List<OpLogVO> voList = result.getRecords().stream()
                .map(OpLogVO::from)
                .collect(Collectors.toList());
        return PageVO.of(result, voList);
    }

    @Override
    public OpLogVO getById(Long id) {
        OpLog oplog = opLogMapper.selectById(id);
        if (oplog == null || oplog.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.OP_LOG_NOT_FOUND);
        }
        return OpLogVO.from(oplog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        OpLog oplog = opLogMapper.selectById(id);
        if (oplog == null || oplog.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.OP_LOG_NOT_FOUND);
        }
        // 软删除：审计日志保留痕迹
        OpLog upd = new OpLog();
        upd.setId(id);
        upd.setDeletedAt(LocalDateTime.now());
        opLogMapper.updateById(upd);
        log.info("[操作日志] 软删除 id={}, module={}, action={}",
                id, oplog.getModule(), oplog.getAction());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanBefore(int retentionDays) {
        if (retentionDays < 1 || retentionDays > 3650) {
            throw new BusinessException(ResultCode.OP_LOG_RETENTION_DAYS_INVALID);
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int rows = opLogMapper.delete(
                new LambdaQueryWrapper<OpLog>()
                        .lt(OpLog::getOpTime, cutoff));
        log.info("[操作日志] 清理 {} 天前的数据，cutoff={}, 删除 {} 条", retentionDays, cutoff, rows);
        return rows;
    }

    /* ===== 内部 ===== */

    private String truncate(String s) {
        if (s == null) return null;
        if (s.length() <= PARAMS_MAX_LENGTH) return s;
        return s.substring(0, PARAMS_MAX_LENGTH) + "...[truncated]";
    }
}

package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.ScenicQueryDTO;
import com.ainanning.ticketing.dto.ScenicSaveDTO;
import com.ainanning.ticketing.entity.Scenic;
import com.ainanning.ticketing.mapper.ScenicMapper;
import com.ainanning.ticketing.service.ScenicService;
import com.ainanning.ticketing.vo.ScenicOptionVO;
import com.ainanning.ticketing.vo.ScenicVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 园区业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>所有写操作使用 {@code @Transactional} 保证事务</li>
 *   <li>软删除通过 {@code deleted_at} 字段，在 wrapper 中显式过滤</li>
 *   <li>业务校验在 Service 层完成，Controller 仅做参数格式校验</li>
 *   <li>关键操作有 INFO 级别日志，便于排查</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenicServiceImpl implements ScenicService {

    private final ScenicMapper scenicMapper;

    /** 状态常量：避免魔数散落各处 */
    private static final String STATUS_OPERATING = "运营中";
    private static final String STATUS_PAUSED    = "暂停运营";

    @Override
    public PageVO<ScenicVO> page(ScenicQueryDTO query) {
        log.info("[园区] 分页查询 keyword={}, status={}, page={}/{}",
                query.getKeyword(), query.getStatus(), query.getPageNum(), query.getPageSize());

        // 1. 构造分页对象
        Page<Scenic> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Scenic> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Scenic::getDeletedAt);              // 软删过滤
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Scenic::getName, query.getKeyword().trim());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Scenic::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Scenic::getSort)
               .orderByDesc(Scenic::getId);

        // 3. 执行分页查询
        Page<Scenic> result = scenicMapper.selectPage(page, wrapper);

        // 4. Entity → VO
        List<ScenicVO> records = result.getRecords().stream()
                .map(ScenicVO::from)
                .collect(Collectors.toList());

        return PageVO.of(result, records);
    }

    @Override
    public ScenicVO getById(Long id) {
        log.info("[园区] 查询详情 id={}", id);
        Scenic entity = scenicMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SCENIC_NOT_FOUND);
        }
        return ScenicVO.from(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ScenicSaveDTO dto) {
        log.info("[园区] 新建 name={}", dto.getName());

        // 1. 名称唯一性校验
        validateNameUnique(dto.getName(), null);

        // 2. DTO → Entity
        Scenic entity = new Scenic();
        BeanUtils.copyProperties(dto, entity);

        // 3. 设置默认值
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus(STATUS_OPERATING);
        }
        if (entity.getProjectCount() == null) entity.setProjectCount(0);
        if (entity.getRuleCount()    == null) entity.setRuleCount(0);
        if (entity.getTicketCount()  == null) entity.setTicketCount(0);
        if (entity.getMonthSales()   == null) entity.setMonthSales(BigDecimal.ZERO);
        if (entity.getSort()         == null) entity.setSort(0);

        // 4. 插入
        scenicMapper.insert(entity);
        log.info("[园区] 新建成功 id={}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ScenicSaveDTO dto) {
        log.info("[园区] 更新 id={}", dto.getId());

        // 1. 校验存在
        Scenic exist = getActiveById(dto.getId());

        // 2. 名称唯一性校验（排除自身）
        validateNameUnique(dto.getName(), dto.getId());

        // 3. 仅 set 允许修改的字段，避免冗余聚合字段（projectCount/ruleCount/ticketCount/monthSales）被 null 覆盖
        Scenic entity = new Scenic();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setIcon(dto.getIcon());
        entity.setIconBg(dto.getIconBg());
        entity.setLevel(dto.getLevel());
        entity.setAddress(dto.getAddress());
        entity.setOpenTime(dto.getOpenTime());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setSort(dto.getSort());

        // 4. 更新
        int rows = scenicMapper.updateById(entity);
        if (rows == 0) {
            log.warn("[园区] 更新未影响行数 id={}", dto.getId());
        }
        log.info("[园区] 更新成功 id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        log.info("[园区] 更新状态 id={} -> {}", id, status);

        // 1. 校验状态值合法性
        if (!STATUS_OPERATING.equals(status) && !STATUS_PAUSED.equals(status)) {
            throw new BusinessException(ResultCode.SCENIC_STATUS_INVALID);
        }
        // 2. 校验存在
        Scenic exist = getActiveById(id);
        // 3. 同状态跳过
        if (status.equals(exist.getStatus())) {
            log.info("[园区] 状态未变化，跳过更新 id={}", id);
            return;
        }
        // 4. 更新
        Scenic entity = new Scenic();
        entity.setId(id);
        entity.setStatus(status);
        scenicMapper.updateById(entity);
        log.info("[园区] 状态更新成功 id={} -> {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info("[园区] 删除 id={}", id);

        // 1. 校验存在
        Scenic exist = getActiveById(id);

        // 2. 业务规则：园区下存在有效票种时禁止删除
        if (exist.getTicketCount() != null && exist.getTicketCount() > 0) {
            throw new BusinessException(ResultCode.SCENIC_HAS_TICKETS);
        }

        // 3. 软删除：显式设置 deleted_at
        Scenic entity = new Scenic();
        entity.setId(id);
        entity.setDeletedAt(LocalDateTime.now());
        scenicMapper.updateById(entity);
        log.info("[园区] 删除成功 id={}", id);
    }

    @Override
    public List<ScenicOptionVO> listOptions() {
        log.info("[园区] 查询下拉选项");
        LambdaQueryWrapper<Scenic> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Scenic::getDeletedAt)
               .eq(Scenic::getStatus, STATUS_OPERATING)
               .orderByDesc(Scenic::getSort)
               .orderByDesc(Scenic::getId);

        return scenicMapper.selectList(wrapper).stream()
                .map(s -> ScenicOptionVO.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .icon(s.getIcon())
                        .build())
                .collect(Collectors.toList());
    }

    /* ====================== 私有方法 ====================== */

    /**
     * 获取有效的园区（不存在或已删除时抛业务异常）
     */
    private Scenic getActiveById(Long id) {
        Scenic entity = scenicMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SCENIC_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 校验园区名称唯一性
     *
     * @param name      待校验名称
     * @param excludeId 修改时排除的园区 ID（新增传 null）
     */
    private void validateNameUnique(String name, Long excludeId) {
        long count = excludeId == null
                ? scenicMapper.countByName(name)
                : scenicMapper.countByNameExcludeId(name, excludeId);
        if (count > 0) {
            log.warn("[园区] 名称已存在 name={}, excludeId={}", name, excludeId);
            throw new BusinessException(ResultCode.SCENIC_NAME_DUPLICATE);
        }
    }
}

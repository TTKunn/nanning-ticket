package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.RuleQueryDTO;
import com.ainanning.ticketing.dto.RuleSaveDTO;
import com.ainanning.ticketing.entity.Rule;
import com.ainanning.ticketing.entity.Scenic;
import com.ainanning.ticketing.mapper.RuleMapper;
import com.ainanning.ticketing.mapper.ScenicMapper;
import com.ainanning.ticketing.service.RuleService;
import com.ainanning.ticketing.vo.RuleOptionVO;
import com.ainanning.ticketing.vo.RuleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目规则业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>所有写操作使用 {@code @Transactional} 保证事务</li>
 *   <li>软删除通过 {@code deleted_at} 字段，在 wrapper 中显式过滤</li>
 *   <li>规则类型、状态、日期范围在 Service 层做业务校验</li>
 *   <li>创建/更新/删除规则后，回写 scenic.rule_count 冗余字段，保持列表数据一致</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final RuleMapper ruleMapper;
    private final ScenicMapper scenicMapper;

    /** 状态常量：避免魔数散落各处 */
    private static final String STATUS_ENABLED  = "启用";
    private static final String STATUS_DISABLED = "禁用";

    /** 规则类型白名单 */
    private static final List<String> TYPE_WHITELIST =
            Arrays.asList("折扣", "免票", "团体", "时段", "限流");

    @Override
    public PageVO<RuleVO> page(RuleQueryDTO query) {
        log.info("[规则] 分页查询 scenicId={}, keyword={}, type={}, status={}, page={}/{}",
                query.getScenicId(), query.getKeyword(), query.getType(), query.getStatus(),
                query.getPageNum(), query.getPageSize());

        // 1. 构造分页对象
        Page<Rule> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Rule> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Rule::getDeletedAt);              // 软删过滤
        if (query.getScenicId() != null) {
            wrapper.eq(Rule::getScenicId, query.getScenicId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Rule::getName, kw).or().like(Rule::getCode, kw));
        }
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(Rule::getType, query.getType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Rule::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Rule::getPriority)
               .orderByDesc(Rule::getId);

        // 3. 执行分页查询
        Page<Rule> result = ruleMapper.selectPage(page, wrapper);

        // 4. Entity → VO（注入 scenicName）
        List<Long> scenicIds = result.getRecords().stream()
                .map(Rule::getScenicId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> scenicNameMap = loadScenicNameMap(scenicIds);

        List<RuleVO> records = result.getRecords().stream()
                .map(e -> {
                    RuleVO vo = RuleVO.from(e);
                    if (vo != null) {
                        vo.setScenicName(scenicNameMap.get(e.getScenicId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return PageVO.of(result, records);
    }

    @Override
    public RuleVO getById(Long id) {
        log.info("[规则] 查询详情 id={}", id);
        Rule entity = ruleMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RULE_NOT_FOUND);
        }
        RuleVO vo = RuleVO.from(entity);
        vo.setScenicName(loadScenicName(entity.getScenicId()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RuleSaveDTO dto) {
        log.info("[规则] 新建 scenicId={}, code={}", dto.getScenicId(), dto.getCode());

        // 1. 业务校验
        validateScenicActive(dto.getScenicId());
        validateType(dto.getType());
        validateStatus(dto.getStatus());
        validateDateRange(dto.getEffectiveFrom(), dto.getEffectiveTo());
        validateCodeUnique(dto.getScenicId(), dto.getCode(), null);

        // 2. DTO → Entity
        Rule entity = new Rule();
        BeanUtils.copyProperties(dto, entity);

        // 3. 设置默认值
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus(STATUS_ENABLED);
        }
        if (entity.getPriority() == null) {
            entity.setPriority(0);
        }

        // 4. 插入
        int rows = ruleMapper.insert(entity);
        if (rows == 0) {
            log.error("[规则] 新建失败 scenicId={}, code={}", dto.getScenicId(), dto.getCode());
            throw new BusinessException(ResultCode.RULE_SAVE_FAILED);
        }

        // 5. 回写 scenic.rule_count
        refreshScenicRuleCount(dto.getScenicId());

        log.info("[规则] 新建成功 id={}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RuleSaveDTO dto) {
        log.info("[规则] 更新 id={}", dto.getId());

        // 1. 校验存在
        Rule exist = getActiveById(dto.getId());

        // 2. 业务校验
        validateScenicActive(dto.getScenicId());
        validateType(dto.getType());
        validateStatus(dto.getStatus());
        validateDateRange(dto.getEffectiveFrom(), dto.getEffectiveTo());
        validateCodeUnique(dto.getScenicId(), dto.getCode(), dto.getId());

        // 3. DTO → Entity（保留 createdAt 等不可变字段）
        Rule entity = new Rule();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(exist.getCreatedAt());

        // 4. 更新
        int rows = ruleMapper.updateById(entity);
        if (rows == 0) {
            log.warn("[规则] 更新未影响行数 id={}", dto.getId());
        }

        // 5. 若园区发生变更，需要回写新旧两边的 rule_count
        if (!exist.getScenicId().equals(dto.getScenicId())) {
            refreshScenicRuleCount(exist.getScenicId());
        }
        refreshScenicRuleCount(dto.getScenicId());

        log.info("[规则] 更新成功 id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        log.info("[规则] 更新状态 id={} -> {}", id, status);

        // 1. 校验状态值合法性
        if (!STATUS_ENABLED.equals(status) && !STATUS_DISABLED.equals(status)) {
            throw new BusinessException(ResultCode.RULE_STATUS_INVALID);
        }
        // 2. 校验存在
        Rule exist = getActiveById(id);
        // 3. 同状态跳过
        if (status.equals(exist.getStatus())) {
            log.info("[规则] 状态未变化，跳过更新 id={}", id);
            return;
        }
        // 4. 更新
        Rule entity = new Rule();
        entity.setId(id);
        entity.setStatus(status);
        ruleMapper.updateById(entity);
        log.info("[规则] 状态更新成功 id={} -> {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info("[规则] 删除 id={}", id);

        // 1. 校验存在
        Rule exist = getActiveById(id);

        // 2. 软删除
        Rule entity = new Rule();
        entity.setId(id);
        entity.setDeletedAt(LocalDateTime.now());
        int rows = ruleMapper.updateById(entity);
        if (rows == 0) {
            log.error("[规则] 删除失败 id={}", id);
            throw new BusinessException(ResultCode.RULE_DELETE_FAILED);
        }

        // 3. 回写 scenic.rule_count
        refreshScenicRuleCount(exist.getScenicId());

        log.info("[规则] 删除成功 id={}", id);
    }

    @Override
    public List<RuleOptionVO> listOptions(Long scenicId) {
        log.info("[规则] 查询下拉选项 scenicId={}", scenicId);
        if (scenicId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Rule> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Rule::getDeletedAt)
               .eq(Rule::getScenicId, scenicId)
               .eq(Rule::getStatus, STATUS_ENABLED)
               .orderByDesc(Rule::getPriority)
               .orderByDesc(Rule::getId);

        return ruleMapper.selectList(wrapper).stream()
                .map(r -> RuleOptionVO.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .code(r.getCode())
                        .type(r.getType())
                        .build())
                .collect(Collectors.toList());
    }

    /* ====================== 私有方法 ====================== */

    /**
     * 获取有效规则（不存在或已删除时抛业务异常）
     */
    private Rule getActiveById(Long id) {
        Rule entity = ruleMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RULE_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 校验所属园区存在且未暂停
     */
    private void validateScenicActive(Long scenicId) {
        Scenic scenic = scenicMapper.selectById(scenicId);
        if (scenic == null || scenic.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SCENIC_NOT_FOUND);
        }
        if ("暂停运营".equals(scenic.getStatus())) {
            throw new BusinessException("园区已暂停运营，不能添加规则");
        }
    }

    /**
     * 校验规则类型白名单
     */
    private void validateType(String type) {
        if (!StringUtils.hasText(type) || !TYPE_WHITELIST.contains(type)) {
            throw new BusinessException(ResultCode.RULE_TYPE_INVALID);
        }
    }

    /**
     * 校验规则状态白名单（status 允许为空，新建时会赋默认值）
     */
    private void validateStatus(String status) {
        if (StringUtils.hasText(status)
                && !STATUS_ENABLED.equals(status)
                && !STATUS_DISABLED.equals(status)) {
            throw new BusinessException(ResultCode.RULE_STATUS_INVALID);
        }
    }

    /**
     * 校验生效日期范围：起始不能晚于结束
     */
    private void validateDateRange(java.time.LocalDate from, java.time.LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(ResultCode.RULE_EFFECTIVE_RANGE_INVALID);
        }
    }

    /**
     * 校验同园区下规则编码唯一
     */
    private void validateCodeUnique(Long scenicId, String code, Long excludeId) {
        long count = excludeId == null
                ? ruleMapper.countByCode(scenicId, code)
                : ruleMapper.countByCodeExcludeId(scenicId, code, excludeId);
        if (count > 0) {
            log.warn("[规则] 编码已存在 scenicId={}, code={}, excludeId={}", scenicId, code, excludeId);
            throw new BusinessException(ResultCode.RULE_CODE_DUPLICATE);
        }
    }

    /**
     * 回写 scenic.rule_count
     */
    private void refreshScenicRuleCount(Long scenicId) {
        if (scenicId == null) {
            return;
        }
        long count = ruleMapper.countActiveByScenicId(scenicId);
        Scenic update = new Scenic();
        update.setId(scenicId);
        update.setRuleCount((int) count);
        scenicMapper.updateById(update);
        log.debug("[规则] 回写 scenic.ruleCount scenicId={} -> {}", scenicId, count);
    }

    /**
     * 加载园区名（用于 VO 注入）
     */
    private String loadScenicName(Long scenicId) {
        if (scenicId == null) {
            return null;
        }
        Scenic scenic = scenicMapper.selectById(scenicId);
        return scenic == null ? null : scenic.getName();
    }

    /**
     * 批量加载园区名映射，避免在循环里逐条查 DB
     */
    private Map<Long, String> loadScenicNameMap(List<Long> scenicIds) {
        if (scenicIds == null || scenicIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<Scenic> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Scenic::getId, scenicIds)
               .isNull(Scenic::getDeletedAt)
               .select(Scenic::getId, Scenic::getName);
        List<Scenic> scenics = scenicMapper.selectList(wrapper);
        Map<Long, String> map = new HashMap<>(scenics.size());
        for (Scenic s : scenics) {
            map.put(s.getId(), s.getName());
        }
        return map;
    }
}

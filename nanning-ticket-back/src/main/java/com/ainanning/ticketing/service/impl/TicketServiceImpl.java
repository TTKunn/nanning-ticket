package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.TicketQueryDTO;
import com.ainanning.ticketing.dto.TicketSaveDTO;
import com.ainanning.ticketing.entity.Rule;
import com.ainanning.ticketing.entity.Scenic;
import com.ainanning.ticketing.entity.Ticket;
import com.ainanning.ticketing.mapper.RuleMapper;
import com.ainanning.ticketing.mapper.ScenicMapper;
import com.ainanning.ticketing.mapper.TicketMapper;
import com.ainanning.ticketing.service.TicketService;
import com.ainanning.ticketing.vo.TicketOptionVO;
import com.ainanning.ticketing.vo.TicketVO;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 票种业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>所有写操作使用 {@code @Transactional} 保证事务</li>
 *   <li>软删除通过 {@code deleted_at} 字段，在 wrapper 中显式过滤</li>
 *   <li>{@code ruleIds} / {@code tags} 以前端数组传入，Service 层与逗号分隔字符串互转</li>
 *   <li>关联规则的有效性在保存时校验，避免悬挂的规则 ID</li>
 *   <li>创建/更新/删除票种后，回写 scenic.ticket_count 冗余字段</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketMapper ticketMapper;
    private final ScenicMapper scenicMapper;
    private final RuleMapper ruleMapper;

    /** 状态常量：避免魔数散落各处 */
    private static final String STATUS_ON_SALE  = "在售";
    private static final String STATUS_OFF_SALE = "停售";

    /** 分类白名单 */
    private static final List<String> CATEGORY_WHITELIST =
            Arrays.asList("单票", "套票", "联票");

    @Override
    public PageVO<TicketVO> page(TicketQueryDTO query) {
        log.info("[票种] 分页查询 scenicId={}, keyword={}, category={}, status={}, page={}/{}",
                query.getScenicId(), query.getKeyword(), query.getCategory(), query.getStatus(),
                query.getPageNum(), query.getPageSize());

        // 1. 校验 scenicId 必传
        if (query.getScenicId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "scenicId 不能为空");
        }

        // 2. 构造分页对象
        Page<Ticket> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 3. 构造查询条件
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Ticket::getDeletedAt);              // 软删过滤
        wrapper.eq(Ticket::getScenicId, query.getScenicId());
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Ticket::getName, kw).or().like(Ticket::getCode, kw));
        }
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(Ticket::getCategory, query.getCategory());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Ticket::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Ticket::getSort)
               .orderByDesc(Ticket::getId);

        // 4. 执行分页查询
        Page<Ticket> result = ticketMapper.selectPage(page, wrapper);

        // 5. Entity → VO（注入 scenicName + ruleNames）
        List<Long> scenicIds = result.getRecords().stream()
                .map(Ticket::getScenicId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> scenicNameMap = loadScenicNameMap(scenicIds);

        List<Long> allRuleIds = result.getRecords().stream()
                .map(Ticket::getRuleIds)
                .filter(StringUtils::hasText)
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> ruleNameMap = loadRuleNameMap(allRuleIds);

        List<TicketVO> records = result.getRecords().stream()
                .map(e -> {
                    TicketVO vo = TicketVO.from(e);
                    if (vo != null) {
                        vo.setScenicName(scenicNameMap.get(e.getScenicId()));
                        vo.setRuleNames(vo.getRuleIds().stream()
                                .map(ruleNameMap::get)
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return PageVO.of(result, records);
    }

    @Override
    public TicketVO getById(Long id) {
        log.info("[票种] 查询详情 id={}", id);
        Ticket entity = ticketMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.TICKET_NOT_FOUND);
        }
        TicketVO vo = TicketVO.from(entity);
        vo.setScenicName(loadScenicName(entity.getScenicId()));
        vo.setRuleNames(loadRuleNames(vo.getRuleIds()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TicketSaveDTO dto) {
        log.info("[票种] 新建 scenicId={}, code={}", dto.getScenicId(), dto.getCode());

        // 1. 业务校验
        validateScenicActive(dto.getScenicId());
        validateCategory(dto.getCategory());
        validateStatus(dto.getStatus());
        validatePrice(dto.getPrice());
        validateCodeUnique(dto.getScenicId(), dto.getCode(), null);
        validateRuleIds(dto.getRuleIds(), dto.getScenicId());

        // 2. DTO → Entity
        Ticket entity = new Ticket();
        BeanUtils.copyProperties(dto, entity);

        // 3. 列表字段 → 逗号分隔字符串
        entity.setTags(joinList(dto.getTags()));
        entity.setRuleIds(joinList(dto.getRuleIds()));
        entity.setRefundable(dto.getRefundable() != null && dto.getRefundable() ? 1 : 0);

        // 4. 设置默认值
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus(STATUS_ON_SALE);
        }
        if (entity.getValidDays() == null) {
            entity.setValidDays(1);
        }
        if (entity.getCostPrice() == null) {
            entity.setCostPrice(BigDecimal.ZERO);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }

        // 5. 插入
        int rows = ticketMapper.insert(entity);
        if (rows == 0) {
            log.error("[票种] 新建失败 scenicId={}, code={}", dto.getScenicId(), dto.getCode());
            throw new BusinessException(ResultCode.TICKET_SAVE_FAILED);
        }

        // 6. 回写 scenic.ticket_count
        refreshScenicTicketCount(dto.getScenicId());

        log.info("[票种] 新建成功 id={}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TicketSaveDTO dto) {
        log.info("[票种] 更新 id={}", dto.getId());

        // 1. 校验存在
        Ticket exist = getActiveById(dto.getId());

        // 2. 业务校验
        validateScenicActive(dto.getScenicId());
        validateCategory(dto.getCategory());
        validateStatus(dto.getStatus());
        validatePrice(dto.getPrice());
        validateCodeUnique(dto.getScenicId(), dto.getCode(), dto.getId());
        validateRuleIds(dto.getRuleIds(), dto.getScenicId());

        // 3. DTO → Entity（保留 createdAt 等不可变字段）
        Ticket entity = new Ticket();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(exist.getCreatedAt());

        // 4. 列表字段 → 逗号分隔字符串
        entity.setTags(joinList(dto.getTags()));
        entity.setRuleIds(joinList(dto.getRuleIds()));
        entity.setRefundable(dto.getRefundable() != null && dto.getRefundable() ? 1 : 0);

        // 5. 更新
        int rows = ticketMapper.updateById(entity);
        if (rows == 0) {
            log.warn("[票种] 更新未影响行数 id={}", dto.getId());
        }

        // 6. 若园区发生变更，需要回写新旧两边的 ticket_count
        if (!exist.getScenicId().equals(dto.getScenicId())) {
            refreshScenicTicketCount(exist.getScenicId());
        }
        refreshScenicTicketCount(dto.getScenicId());

        log.info("[票种] 更新成功 id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        log.info("[票种] 更新状态 id={} -> {}", id, status);

        // 1. 校验状态值合法性
        if (!STATUS_ON_SALE.equals(status) && !STATUS_OFF_SALE.equals(status)) {
            throw new BusinessException(ResultCode.TICKET_STATUS_INVALID);
        }
        // 2. 校验存在
        Ticket exist = getActiveById(id);
        // 3. 同状态跳过
        if (status.equals(exist.getStatus())) {
            log.info("[票种] 状态未变化，跳过更新 id={}", id);
            return;
        }
        // 4. 更新
        Ticket entity = new Ticket();
        entity.setId(id);
        entity.setStatus(status);
        ticketMapper.updateById(entity);
        log.info("[票种] 状态更新成功 id={} -> {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info("[票种] 删除 id={}", id);

        // 1. 校验存在
        Ticket exist = getActiveById(id);

        // 2. 业务规则：票种下存在有效订单时禁止删除（占位）
        //    注：订单模块尚未开发，待对接 order 表后改为真实统计
        //    现阶段业务预占，若需要软删除订单可绕过此检查
        if (hasOrders(exist.getId())) {
            throw new BusinessException(ResultCode.TICKET_HAS_ORDERS);
        }

        // 3. 软删除
        Ticket entity = new Ticket();
        entity.setId(id);
        entity.setDeletedAt(LocalDateTime.now());
        int rows = ticketMapper.updateById(entity);
        if (rows == 0) {
            log.error("[票种] 删除失败 id={}", id);
            throw new BusinessException(ResultCode.TICKET_DELETE_FAILED);
        }

        // 4. 回写 scenic.ticket_count
        refreshScenicTicketCount(exist.getScenicId());

        log.info("[票种] 删除成功 id={}", id);
    }

    @Override
    public List<TicketOptionVO> listOptions(Long scenicId) {
        log.info("[票种] 查询下拉选项 scenicId={}", scenicId);
        if (scenicId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Ticket::getDeletedAt)
               .eq(Ticket::getScenicId, scenicId)
               .eq(Ticket::getStatus, STATUS_ON_SALE)
               .orderByDesc(Ticket::getSort)
               .orderByDesc(Ticket::getId);

        List<Ticket> tickets = ticketMapper.selectList(wrapper);
        if (tickets.isEmpty()) {
            return Collections.emptyList();
        }
        // 注入 scenicName
        String scenicName = loadScenicName(scenicId);
        return tickets.stream()
                .map(t -> TicketOptionVO.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .code(t.getCode())
                        .price(t.getPrice())
                        .scenicId(t.getScenicId())
                        .scenicName(scenicName)
                        .category(t.getCategory())
                        .build())
                .collect(Collectors.toList());
    }

    /* ====================== 私有方法 ====================== */

    /** 获取有效票种（不存在或已删除时抛业务异常） */
    private Ticket getActiveById(Long id) {
        Ticket entity = ticketMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.TICKET_NOT_FOUND);
        }
        return entity;
    }

    /** 校验所属园区存在且未暂停 */
    private void validateScenicActive(Long scenicId) {
        Scenic scenic = scenicMapper.selectById(scenicId);
        if (scenic == null || scenic.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SCENIC_NOT_FOUND);
        }
        if ("暂停运营".equals(scenic.getStatus())) {
            throw new BusinessException("园区已暂停运营，不能添加票种");
        }
    }

    /** 校验分类白名单 */
    private void validateCategory(String category) {
        if (!StringUtils.hasText(category) || !CATEGORY_WHITELIST.contains(category)) {
            throw new BusinessException(ResultCode.TICKET_CATEGORY_INVALID);
        }
    }

    /** 校验状态白名单（status 允许为空，新建时会赋默认值） */
    private void validateStatus(String status) {
        if (StringUtils.hasText(status)
                && !STATUS_ON_SALE.equals(status)
                && !STATUS_OFF_SALE.equals(status)) {
            throw new BusinessException(ResultCode.TICKET_STATUS_INVALID);
        }
    }

    /** 校验票面价：非空且 ≥ 0（0 表示免费票） */
    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new BusinessException(ResultCode.TICKET_PRICE_INVALID);
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.TICKET_PRICE_INVALID);
        }
    }

    /** 校验同园区下票种编码唯一 */
    private void validateCodeUnique(Long scenicId, String code, Long excludeId) {
        long count = excludeId == null
                ? ticketMapper.countByCode(scenicId, code)
                : ticketMapper.countByCodeExcludeId(scenicId, code, excludeId);
        if (count > 0) {
            log.warn("[票种] 编码已存在 scenicId={}, code={}, excludeId={}", scenicId, code, excludeId);
            throw new BusinessException(ResultCode.TICKET_CODE_DUPLICATE);
        }
    }

    /**
     * 校验关联规则：所有 ruleIds 必须存在、属于同一园区、未被软删除
     */
    private void validateRuleIds(List<Long> ruleIds, Long scenicId) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<Rule> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Rule::getId, ruleIds)
               .isNull(Rule::getDeletedAt)
               .eq(Rule::getScenicId, scenicId);
        List<Rule> existRules = ruleMapper.selectList(wrapper);
        if (existRules.size() != ruleIds.size()) {
            log.warn("[票种] 关联规则校验失败 请求={}, 实际存在={}", ruleIds.size(), existRules.size());
            throw new BusinessException(ResultCode.TICKET_RULE_NOT_FOUND);
        }
    }

    /** 票种下是否存在有效订单（占位：订单模块上线后改为真实统计） */
    private boolean hasOrders(Long ticketId) {
        // TODO 订单模块开发完成后实现：
        //   1. 查 order 表 WHERE ticket_id = #{ticketId} AND deleted_at IS NULL LIMIT 1
        //   2. 存在则返回 true
        return false;
    }

    /** 回写 scenic.ticket_count */
    private void refreshScenicTicketCount(Long scenicId) {
        if (scenicId == null) {
            return;
        }
        long count = ticketMapper.countActiveByScenicId(scenicId);
        Scenic update = new Scenic();
        update.setId(scenicId);
        update.setTicketCount((int) count);
        scenicMapper.updateById(update);
        log.debug("[票种] 回写 scenic.ticketCount scenicId={} -> {}", scenicId, count);
    }

    /** 加载单个园区名（用于 VO 注入） */
    private String loadScenicName(Long scenicId) {
        if (scenicId == null) {
            return null;
        }
        Scenic scenic = scenicMapper.selectById(scenicId);
        return scenic == null ? null : scenic.getName();
    }

    /** 批量加载园区名映射，避免循环里逐条查 DB */
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

    /** 加载单个规则的名称列表（用于 VO 注入） */
    private List<String> loadRuleNames(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Rule> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Rule::getId, ruleIds)
               .isNull(Rule::getDeletedAt)
               .select(Rule::getId, Rule::getName);
        Map<Long, String> nameMap = new HashMap<>();
        ruleMapper.selectList(wrapper).forEach(r -> nameMap.put(r.getId(), r.getName()));
        return ruleIds.stream()
                .map(nameMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** 批量加载规则名映射 */
    private Map<Long, String> loadRuleNameMap(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<Rule> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Rule::getId, ruleIds)
               .isNull(Rule::getDeletedAt)
               .select(Rule::getId, Rule::getName);
        Map<Long, String> map = new HashMap<>();
        ruleMapper.selectList(wrapper).forEach(r -> map.put(r.getId(), r.getName()));
        return map;
    }

    /** 列表 → 逗号分隔字符串（过滤 null/空白） */
    private String joinList(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(java.util.Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }
}

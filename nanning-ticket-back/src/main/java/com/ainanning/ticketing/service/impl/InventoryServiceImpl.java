package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.InventoryBatchDTO;
import com.ainanning.ticketing.dto.InventoryQueryDTO;
import com.ainanning.ticketing.dto.InventorySaveDTO;
import com.ainanning.ticketing.entity.Inventory;
import com.ainanning.ticketing.entity.Scenic;
import com.ainanning.ticketing.entity.Ticket;
import com.ainanning.ticketing.mapper.InventoryMapper;
import com.ainanning.ticketing.mapper.ScenicMapper;
import com.ainanning.ticketing.mapper.TicketMapper;
import com.ainanning.ticketing.service.InventoryService;
import com.ainanning.ticketing.vo.InventoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 库存业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>所有写操作使用 {@code @Transactional} 保证事务</li>
 *   <li>"票种 × 日期" 唯一性由 Service 层校验（{@code isNull(deletedAt)} 过滤），
 *       DB 唯一键作为最后防线</li>
 *   <li>批量创建时跳过已存在日期，而非整体回滚，提升用户体验</li>
 *   <li>已售（{@code sold > 0}）的库存不允许删除 / 修改 total，避免数据回滚</li>
 *   <li>{@code available} 与售罄状态在 VO 层实时计算，DB 字段值仅作持久化参考</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final TicketMapper ticketMapper;
    private final ScenicMapper scenicMapper;

    /** 状态常量：避免魔数散落各处 */
    private static final String STATUS_OPEN    = "开放";
    private static final String STATUS_CLOSE   = "关闭";
    private static final String STATUS_SOLDOUT = "售罄";

    /** 批量创建的最大日期跨度 */
    private static final int MAX_BATCH_DAYS = 365;

    @Override
    public PageVO<InventoryVO> page(InventoryQueryDTO query) {
        log.info("[库存] 分页查询 ticketId={}, scenicId={}, dateFrom={}, dateTo={}, status={}, page={}/{}",
                query.getTicketId(), query.getScenicId(),
                query.getDateFrom(), query.getDateTo(), query.getStatus(),
                query.getPageNum(), query.getPageSize());

        // 1. 构造分页对象
        Page<Inventory> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Inventory::getDeletedAt);              // 软删过滤
        if (query.getTicketId() != null) {
            wrapper.eq(Inventory::getTicketId, query.getTicketId());
        }
        if (query.getScenicId() != null) {
            wrapper.eq(Inventory::getScenicId, query.getScenicId());
        }
        if (query.getDateFrom() != null) {
            wrapper.ge(Inventory::getInventoryDate, query.getDateFrom());
        }
        if (query.getDateTo() != null) {
            wrapper.le(Inventory::getInventoryDate, query.getDateTo());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Inventory::getStatus, query.getStatus());
        }
        // 默认按日期升序（库存更适合"日历视图"）
        wrapper.orderByAsc(Inventory::getInventoryDate)
               .orderByAsc(Inventory::getId);

        // 3. 执行分页查询
        Page<Inventory> result = inventoryMapper.selectPage(page, wrapper);

        // 4. Entity → VO（注入 ticketName / scenicName）
        List<InventoryVO> records = enrichRecords(result.getRecords());

        return PageVO.of(result, records);
    }

    @Override
    public InventoryVO getById(Long id) {
        log.info("[库存] 查询详情 id={}", id);
        Inventory entity = inventoryMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }
        return enrichRecords(Collections.singletonList(entity)).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(InventorySaveDTO dto) {
        log.info("[库存] 新建 ticketId={}, date={}, total={}",
                dto.getTicketId(), dto.getInventoryDate(), dto.getTotal());

        // 1. 校验票种有效
        Ticket ticket = validateTicketActive(dto.getTicketId());

        // 2. 业务校验
        validateDate(dto.getInventoryDate());
        validateTotal(dto.getTotal());
        validateStatus(dto.getStatus());
        validateDateUnique(dto.getTicketId(), dto.getInventoryDate(), null);

        // 3. 构造 Entity
        Inventory entity = new Inventory();
        BeanUtils.copyProperties(dto, entity);
        // 冗余 scenicId（从票种获取）
        entity.setScenicId(ticket.getScenicId());
        // 销售/预占/可用初始值
        entity.setSold(0);
        entity.setReserved(0);
        entity.setAvailable(dto.getTotal());
        // 默认状态
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus(STATUS_OPEN);
        }

        // 4. 插入
        int rows = inventoryMapper.insert(entity);
        if (rows == 0) {
            log.error("[库存] 新建失败 ticketId={}, date={}", dto.getTicketId(), dto.getInventoryDate());
            throw new BusinessException(ResultCode.INVENTORY_SAVE_FAILED);
        }

        log.info("[库存] 新建成功 id={}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createBatch(InventoryBatchDTO dto) {
        log.info("[库存] 批量新建 ticketId={}, [{} ~ {}], total={}",
                dto.getTicketId(), dto.getStartDate(), dto.getEndDate(), dto.getTotal());

        // 1. 校验日期范围
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BusinessException(ResultCode.INVENTORY_DATE_INVALID, "起始日期不能晚于结束日期");
        }
        long span = dto.getStartDate().toEpochDay() - dto.getEndDate().toEpochDay();
        // 负数 + 1 = 天数
        if (Math.abs(span) + 1 > MAX_BATCH_DAYS) {
            throw new BusinessException(ResultCode.INVENTORY_DATE_RANGE_TOO_LARGE,
                    "日期范围不能超过 " + MAX_BATCH_DAYS + " 天");
        }

        // 2. 校验票种有效
        Ticket ticket = validateTicketActive(dto.getTicketId());

        // 3. 找出已存在的日期（同票种 + 在区间内 + 未软删）
        LambdaQueryWrapper<Inventory> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.isNull(Inventory::getDeletedAt)
                    .eq(Inventory::getTicketId, dto.getTicketId())
                    .between(Inventory::getInventoryDate, dto.getStartDate(), dto.getEndDate())
                    .select(Inventory::getInventoryDate);
        List<Inventory> existList = inventoryMapper.selectList(existWrapper);
        Set<LocalDate> existDates = existList.stream()
                .map(Inventory::getInventoryDate)
                .collect(Collectors.toCollection(HashSet::new));

        // 4. 构造待插入列表
        List<Inventory> toInsert = new ArrayList<>();
        LocalDate cursor = dto.getStartDate();
        while (!cursor.isAfter(dto.getEndDate())) {
            if (!existDates.contains(cursor)) {
                Inventory inv = new Inventory();
                inv.setTicketId(dto.getTicketId());
                inv.setScenicId(ticket.getScenicId());
                inv.setInventoryDate(cursor);
                inv.setTotal(dto.getTotal());
                inv.setSold(0);
                inv.setReserved(0);
                inv.setAvailable(dto.getTotal());
                inv.setStatus(STATUS_OPEN);
                inv.setRemark(dto.getRemark());
                toInsert.add(inv);
            }
            cursor = cursor.plusDays(1);
        }

        if (toInsert.isEmpty()) {
            log.info("[库存] 批量新建无新增（区间内全部已存在） ticketId={}", dto.getTicketId());
            return 0;
        }

        // 5. 批量插入（MyBatis-Plus 暂无 batchInsert，单条插入；量小时可接受）
        int successCount = 0;
        for (Inventory inv : toInsert) {
            int rows = inventoryMapper.insert(inv);
            if (rows > 0) {
                successCount++;
            }
        }

        log.info("[库存] 批量新建完成 期望={}, 成功={}, 跳过(已存在)={}",
                toInsert.size(), successCount, existDates.size());
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(InventorySaveDTO dto) {
        log.info("[库存] 更新 id={}", dto.getId());

        // 1. 校验存在
        Inventory exist = getActiveById(dto.getId());

        // 2. 业务校验：已售记录的库存不允许修改 total
        if (dto.getTotal() != null && dto.getTotal() < exist.getSold()) {
            throw new BusinessException(ResultCode.INVENTORY_TOTAL_INVALID,
                    "总库存不能小于已售数量 (" + exist.getSold() + ")");
        }

        // 3. 业务校验：日期与票种不能修改
        if (dto.getTicketId() != null && !dto.getTicketId().equals(exist.getTicketId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不允许修改票种");
        }
        if (dto.getInventoryDate() != null && !dto.getInventoryDate().equals(exist.getInventoryDate())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不允许修改库存日期");
        }

        // 4. 校验新状态
        validateStatus(dto.getStatus());

        // 5. DTO → Entity
        Inventory entity = new Inventory();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreatedAt(exist.getCreatedAt());

        // 6. 重新计算 available
        if (dto.getTotal() != null) {
            int sold     = exist.getSold()     == null ? 0 : exist.getSold();
            int reserved = exist.getReserved() == null ? 0 : exist.getReserved();
            entity.setAvailable(Math.max(0, dto.getTotal() - sold - reserved));
        }

        // 7. 更新
        int rows = inventoryMapper.updateById(entity);
        if (rows == 0) {
            log.warn("[库存] 更新未影响行数 id={}", dto.getId());
        }
        log.info("[库存] 更新成功 id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        log.info("[库存] 更新状态 id={} -> {}", id, status);

        // 1. 校验状态值合法性
        if (!STATUS_OPEN.equals(status) && !STATUS_CLOSE.equals(status) && !STATUS_SOLDOUT.equals(status)) {
            throw new BusinessException(ResultCode.INVENTORY_STATUS_INVALID);
        }
        // 2. 校验存在
        Inventory exist = getActiveById(id);
        // 3. 同状态跳过
        if (status.equals(exist.getStatus())) {
            log.info("[库存] 状态未变化，跳过更新 id={}", id);
            return;
        }
        // 4. 更新
        Inventory entity = new Inventory();
        entity.setId(id);
        entity.setStatus(status);
        inventoryMapper.updateById(entity);
        log.info("[库存] 状态更新成功 id={} -> {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info("[库存] 删除 id={}", id);

        // 1. 校验存在
        Inventory exist = getActiveById(id);

        // 2. 业务规则：已售记录的库存不允许删除
        if (exist.getSold() != null && exist.getSold() > 0) {
            throw new BusinessException(ResultCode.INVENTORY_HAS_SOLD);
        }

        // 3. 软删除
        Inventory entity = new Inventory();
        entity.setId(id);
        entity.setDeletedAt(LocalDateTime.now());
        int rows = inventoryMapper.updateById(entity);
        if (rows == 0) {
            log.error("[库存] 删除失败 id={}", id);
            throw new BusinessException(ResultCode.INVENTORY_DELETE_FAILED);
        }
        log.info("[库存] 删除成功 id={}", id);
    }

    /* ====================== 私有方法 ====================== */

    /**
     * 获取有效库存（不存在或已删除时抛业务异常）
     */
    private Inventory getActiveById(Long id) {
        Inventory entity = inventoryMapper.selectById(id);
        if (entity == null || entity.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 校验票种存在且未软删
     */
    private Ticket validateTicketActive(Long ticketId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || ticket.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.TICKET_NOT_FOUND);
        }
        return ticket;
    }

    /**
     * 校验库存日期
     */
    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new BusinessException(ResultCode.INVENTORY_DATE_INVALID, "库存日期不能为空");
        }
    }

    /**
     * 校验总库存：≥ 0（0 表示不售，仅供记录用）
     */
    private void validateTotal(Integer total) {
        if (total == null || total < 0) {
            throw new BusinessException(ResultCode.INVENTORY_TOTAL_INVALID);
        }
    }

    /**
     * 校验状态白名单（status 允许为空，新建时会赋默认值）
     */
    private void validateStatus(String status) {
        if (StringUtils.hasText(status)
                && !STATUS_OPEN.equals(status)
                && !STATUS_CLOSE.equals(status)
                && !STATUS_SOLDOUT.equals(status)) {
            throw new BusinessException(ResultCode.INVENTORY_STATUS_INVALID);
        }
    }

    /**
     * 校验同票种同日期唯一
     */
    private void validateDateUnique(Long ticketId, LocalDate date, Long excludeId) {
        long count = excludeId == null
                ? inventoryMapper.countByTicketAndDate(ticketId, date)
                : inventoryMapper.countByTicketAndDateExcludeId(ticketId, date, excludeId);
        if (count > 0) {
            log.warn("[库存] 日期已存在 ticketId={}, date={}, excludeId={}", ticketId, date, excludeId);
            throw new BusinessException(ResultCode.INVENTORY_DATE_DUPLICATE);
        }
    }

    /**
     * Entity 列表 → VO 列表，注入 ticketName / scenicName
     */
    private List<InventoryVO> enrichRecords(List<Inventory> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. 收集所有需要查的 ticketId
        Set<Long> ticketIds = entities.stream()
                .map(Inventory::getTicketId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (ticketIds.isEmpty()) {
            return entities.stream().map(InventoryVO::from).collect(Collectors.toList());
        }
        // 2. 批量查 ticket（拿 ticketName + scenicId）
        LambdaQueryWrapper<Ticket> ticketWrapper = new LambdaQueryWrapper<>();
        ticketWrapper.in(Ticket::getId, ticketIds)
                     .isNull(Ticket::getDeletedAt)
                     .select(Ticket::getId, Ticket::getName, Ticket::getScenicId);
        List<Ticket> tickets = ticketMapper.selectList(ticketWrapper);
        Map<Long, Ticket> ticketMap = tickets.stream()
                .collect(Collectors.toMap(Ticket::getId, t -> t));

        // 3. 批量查 scenic（拿 scenicName）
        Set<Long> scenicIds = tickets.stream()
                .map(Ticket::getScenicId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> scenicNameMap = new HashMap<>();
        if (!scenicIds.isEmpty()) {
            LambdaQueryWrapper<Scenic> scenicWrapper = new LambdaQueryWrapper<>();
            scenicWrapper.in(Scenic::getId, scenicIds)
                         .isNull(Scenic::getDeletedAt)
                         .select(Scenic::getId, Scenic::getName);
            scenicMapper.selectList(scenicWrapper).forEach(s -> scenicNameMap.put(s.getId(), s.getName()));
        }

        // 4. Entity → VO + 注入
        return entities.stream().map(e -> {
            InventoryVO vo = InventoryVO.from(e);
            if (vo != null) {
                Ticket t = ticketMap.get(e.getTicketId());
                if (t != null) {
                    vo.setTicketName(t.getName());
                    // 优先用 ticket 的 scenicId（数据源更可靠）
                    vo.setScenicId(t.getScenicId());
                    vo.setScenicName(scenicNameMap.get(t.getScenicId()));
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }
}

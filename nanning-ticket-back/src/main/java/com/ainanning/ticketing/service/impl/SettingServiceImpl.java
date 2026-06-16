package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.SettingQueryDTO;
import com.ainanning.ticketing.dto.SettingSaveDTO;
import com.ainanning.ticketing.entity.Setting;
import com.ainanning.ticketing.mapper.SettingMapper;
import com.ainanning.ticketing.service.SettingService;
import com.ainanning.ticketing.vo.SettingVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统参数业务实现
 *
 * <p>设计要点：
 * <ul>
 *   <li>key 唯一性约束（uk_setting_key）+ settingKey 格式校验（{@code ^[A-Za-z][A-Za-z0-9_.]{1,63}$}）</li>
 *   <li>valueType 在 save() 时校验值合法性：NUMBER 用 {@code BigDecimal} 解析、BOOLEAN 用 {@code Boolean.parseBoolean}、JSON 用 Jackson</li>
 *   <li>isReadonly=1 的参数 save() 直接拒绝（{@code SETTING_READONLY}）</li>
 *   <li>静态读取入口（{@code getString/getInt/getBool/getJson}）走 DB 简单查询；
 *       未来可改 Redis 缓存——但当前原型按"直查"实现</li>
 *   <li>删除限制：仅 isReadonly=0 可删，避免误删系统内置参数</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final SettingMapper settingMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> VALID_GROUPS = Set.of(
            Setting.GROUP_GENERIC, Setting.GROUP_ORDER, Setting.GROUP_PAYMENT,
            Setting.GROUP_TICKET, Setting.GROUP_CHANNEL, Setting.GROUP_SCENIC);

    private static final Set<String> VALID_TYPES = Set.of(
            Setting.TYPE_STRING, Setting.TYPE_NUMBER, Setting.TYPE_BOOLEAN, Setting.TYPE_JSON);

    @Override
    public PageVO<SettingVO> page(SettingQueryDTO query) {
        log.info("[设置] 分页查询 keyword={}, group={}, type={}, status={}",
                query.getKeyword(), query.getGroupName(), query.getValueType(), query.getStatus());

        Page<Setting> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Setting> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Setting::getDeletedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Setting::getSettingKey, kw)
                    .or().like(Setting::getDescription, kw));
        }
        if (StringUtils.hasText(query.getGroupName())) {
            wrapper.eq(Setting::getGroupName, query.getGroupName());
        }
        if (StringUtils.hasText(query.getValueType())) {
            wrapper.eq(Setting::getValueType, query.getValueType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Setting::getStatus, query.getStatus());
        }
        if (query.getIsReadonly() != null) {
            wrapper.eq(Setting::getIsReadonly, query.getIsReadonly());
        }
        wrapper.orderByAsc(Setting::getGroupName)
                .orderByAsc(Setting::getId);

        Page<Setting> result = settingMapper.selectPage(page, wrapper);
        List<SettingVO> voList = result.getRecords().stream()
                .map(SettingVO::from)
                .collect(Collectors.toList());
        return PageVO.of(result, voList);
    }

    @Override
    public SettingVO getById(Long id) {
        Setting setting = settingMapper.selectById(id);
        if (setting == null || setting.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SETTING_NOT_FOUND);
        }
        return SettingVO.from(setting);
    }

    @Override
    public SettingVO getByKey(String settingKey) {
        Setting setting = selectActiveByKey(settingKey);
        if (setting == null) {
            throw new BusinessException(ResultCode.SETTING_NOT_FOUND);
        }
        return SettingVO.from(setting);
    }

    @Override
    public List<SettingVO> listByGroup(String groupName) {
        List<Setting> list = settingMapper.selectList(
                new LambdaQueryWrapper<Setting>()
                        .eq(Setting::getGroupName, groupName)
                        .eq(Setting::getStatus, Setting.STATUS_ENABLED)
                        .isNull(Setting::getDeletedAt)
                        .orderByAsc(Setting::getId));
        return list.stream().map(SettingVO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(SettingSaveDTO dto) {
        // 1. 分组 / 类型校验
        if (!VALID_GROUPS.contains(dto.getGroupName())) {
            throw new BusinessException(ResultCode.SETTING_GROUP_INVALID);
        }
        if (!VALID_TYPES.contains(dto.getValueType())) {
            throw new BusinessException(ResultCode.SETTING_VALUE_INVALID);
        }
        // 2. 值与类型一致性校验
        validateValue(dto.getValueType(), dto.getSettingValue());
        // 3. 状态兜底
        if (!StringUtils.hasText(dto.getStatus())) {
            dto.setStatus(Setting.STATUS_ENABLED);
        }

        if (dto.getId() == null) {
            // 新增：key 唯一性
            if (selectActiveByKey(dto.getSettingKey()) != null) {
                throw new BusinessException(ResultCode.SETTING_KEY_DUPLICATE);
            }
            Setting entity = new Setting();
            BeanUtils.copyProperties(dto, entity);
            if (entity.getIsReadonly() == null) entity.setIsReadonly(0);
            settingMapper.insert(entity);
            log.info("[设置] 新增 key={}, value={}", entity.getSettingKey(), entity.getSettingValue());
            return entity.getId();
        } else {
            // 修改：只读拦截
            Setting existing = settingMapper.selectById(dto.getId());
            if (existing == null || existing.getDeletedAt() != null) {
                throw new BusinessException(ResultCode.SETTING_NOT_FOUND);
            }
            if (Integer.valueOf(1).equals(existing.getIsReadonly())) {
                throw new BusinessException(ResultCode.SETTING_READONLY);
            }
            // key 改了 → 新 key 唯一性
            if (!existing.getSettingKey().equals(dto.getSettingKey())) {
                if (selectActiveByKey(dto.getSettingKey()) != null) {
                    throw new BusinessException(ResultCode.SETTING_KEY_DUPLICATE);
                }
            }
            BeanUtils.copyProperties(dto, existing);
            // 只读字段不可通过 save 修改
            existing.setIsReadonly(0);
            settingMapper.updateById(existing);
            log.info("[设置] 修改 id={}, key={}", existing.getId(), existing.getSettingKey());
            return existing.getId();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        if (!Setting.STATUS_ENABLED.equals(status) && !Setting.STATUS_DISABLED.equals(status)) {
            throw new BusinessException(ResultCode.SETTING_STATUS_INVALID);
        }
        Setting setting = settingMapper.selectById(id);
        if (setting == null || setting.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SETTING_NOT_FOUND);
        }
        // 只读参数不允许修改任何字段
        if (Integer.valueOf(1).equals(setting.getIsReadonly())) {
            throw new BusinessException(ResultCode.SETTING_READONLY);
        }
        setting.setStatus(status);
        settingMapper.updateById(setting);
        log.info("[设置] 切状态 id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        Setting setting = settingMapper.selectById(id);
        if (setting == null || setting.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SETTING_NOT_FOUND);
        }
        if (Integer.valueOf(1).equals(setting.getIsReadonly())) {
            throw new BusinessException(ResultCode.SETTING_READONLY);
        }
        // 软删除：与其他模块保持一致
        Setting upd = new Setting();
        upd.setId(id);
        upd.setDeletedAt(java.time.LocalDateTime.now());
        settingMapper.updateById(upd);
        log.info("[设置] 软删除 id={}, key={}", id, setting.getSettingKey());
    }

    /* ===== 业务侧静态取值 ===== */

    @Override
    public String getString(String key, String defaultValue) {
        Setting s = selectActiveByKey(key);
        if (s == null) return defaultValue;
        if (!Setting.STATUS_ENABLED.equals(s.getStatus())) return defaultValue;
        return s.getSettingValue() == null ? defaultValue : s.getSettingValue();
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
        Setting s = selectActiveByKey(key);
        if (s == null || !Setting.STATUS_ENABLED.equals(s.getStatus())
                || !Setting.TYPE_NUMBER.equals(s.getValueType())
                || s.getSettingValue() == null) {
            return defaultValue;
        }
        try {
            // 支持整数 / 小数；这里用整型
            return new java.math.BigDecimal(s.getSettingValue()).intValue();
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Boolean getBool(String key, Boolean defaultValue) {
        Setting s = selectActiveByKey(key);
        if (s == null || !Setting.STATUS_ENABLED.equals(s.getStatus())
                || !Setting.TYPE_BOOLEAN.equals(s.getValueType())
                || s.getSettingValue() == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(s.getSettingValue());
    }

    @Override
    public String getJson(String key, String defaultValue) {
        Setting s = selectActiveByKey(key);
        if (s == null || !Setting.STATUS_ENABLED.equals(s.getStatus())) return defaultValue;
        return s.getSettingValue() == null ? defaultValue : s.getSettingValue();
    }

    /* ===== 内部 ===== */

    private Setting selectActiveByKey(String key) {
        return settingMapper.selectOne(
                new LambdaQueryWrapper<Setting>()
                        .eq(Setting::getSettingKey, key)
                        .isNull(Setting::getDeletedAt)
                        .last("LIMIT 1"));
    }

    /** 校验 value 与 valueType 是否匹配 */
    private void validateValue(String valueType, String value) {
        if (value == null) return;
        switch (valueType) {
            case Setting.TYPE_NUMBER -> {
                try {
                    new java.math.BigDecimal(value);
                } catch (NumberFormatException e) {
                    throw new BusinessException(ResultCode.SETTING_VALUE_INVALID,
                            "NUMBER 类型值无法解析为数字");
                }
            }
            case Setting.TYPE_BOOLEAN -> {
                String v = value.toLowerCase();
                if (!"true".equals(v) && !"false".equals(v)) {
                    throw new BusinessException(ResultCode.SETTING_VALUE_INVALID,
                            "BOOLEAN 类型值必须为 true / false");
                }
            }
            case Setting.TYPE_JSON -> {
                try {
                    objectMapper.readTree(value);
                } catch (Exception e) {
                    throw new BusinessException(ResultCode.SETTING_VALUE_INVALID,
                            "JSON 类型值无法解析为合法 JSON");
                }
            }
            case Setting.TYPE_STRING -> { /* STRING 不校验 */ }
            default -> throw new BusinessException(ResultCode.SETTING_VALUE_INVALID);
        }
    }
}

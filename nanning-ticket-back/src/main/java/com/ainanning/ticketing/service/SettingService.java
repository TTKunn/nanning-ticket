package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.SettingQueryDTO;
import com.ainanning.ticketing.dto.SettingSaveDTO;
import com.ainanning.ticketing.vo.SettingVO;

import java.util.List;

/**
 * 系统参数业务接口
 *
 * <p>系统参数 = 通用 key-value 全局配置，存于 {@code setting} 表。
 * 业务侧推荐使用 {@link #getString} / {@link #getInt} / {@link #getBool} / {@link #getJson} 静态读取
 * （内部走缓存 + 库兜底，原型阶段不引入 Redis 缓存）。</p>
 *
 * @author nanning-ticket
 */
public interface SettingService {

    /** 分页查询 */
    PageVO<SettingVO> page(SettingQueryDTO query);

    /** 详情 */
    SettingVO getById(Long id);

    /** 按 key 查（未命中抛 {@code SETTING_NOT_FOUND}） */
    SettingVO getByKey(String settingKey);

    /** 按 group 批量取（按 sort 不支持，保持插入顺序） */
    List<SettingVO> listByGroup(String groupName);

    /** 新增 / 修改（id 为空 = 新增） */
    Long save(SettingSaveDTO dto);

    /** 切换状态（启用 / 停用） */
    void updateStatus(Long id, String status);

    /** 删除（仅非只读可删） */
    void deleteById(Long id);

    /* ===== 业务侧静态取值入口（弱类型，依赖业务方传入默认值） ===== */

    /** 取字符串值；命中失败返回 {@code defaultValue} */
    String getString(String key, String defaultValue);

    /** 取整数值；命中失败 / 解析失败返回 {@code defaultValue} */
    Integer getInt(String key, Integer defaultValue);

    /** 取布尔值；命中失败 / 解析失败返回 {@code defaultValue} */
    Boolean getBool(String key, Boolean defaultValue);

    /** 取 JSON 原始字符串；命中失败返回 {@code defaultValue} */
    String getJson(String key, String defaultValue);
}

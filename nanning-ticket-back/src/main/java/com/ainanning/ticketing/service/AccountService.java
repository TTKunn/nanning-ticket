package com.ainanning.ticketing.service;

import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.UserQueryDTO;
import com.ainanning.ticketing.dto.UserSaveDTO;
import com.ainanning.ticketing.vo.UserVO;

import java.util.List;

/**
 * 账号管理服务（用户 CRUD + 启停）
 *
 * @author nanning-ticket
 */
public interface AccountService {

    /** 分页查询用户 */
    PageVO<UserVO> page(UserQueryDTO query);

    /** 获取用户详情 */
    UserVO getById(Long id);

    /** 新增用户 */
    Long create(UserSaveDTO dto);

    /** 修改用户 */
    void update(UserSaveDTO dto);

    /** 删除用户（软删除） */
    void deleteById(Long id);

    /** 重置指定用户密码（管理员操作） */
    void resetPassword(Long id, String newPassword);

    /** 启停用户 */
    void updateStatus(Long id, String status);

    /** 获取启用的用户下拉列表（用于"操作人"等下拉） */
    List<UserVO> listEnabled();
}

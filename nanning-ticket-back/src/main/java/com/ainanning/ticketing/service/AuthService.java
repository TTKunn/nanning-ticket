package com.ainanning.ticketing.service;

import com.ainanning.ticketing.dto.ChangePasswordDTO;
import com.ainanning.ticketing.dto.LoginDTO;
import com.ainanning.ticketing.vo.LoginUserVO;
import com.ainanning.ticketing.vo.UserVO;

/**
 * 认证服务
 *
 * @author nanning-ticket
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param dto      登录参数
     * @param clientIp 客户端 IP
     * @return 登录结果（令牌 + 当前用户）
     */
    LoginUserVO login(LoginDTO dto, String clientIp);

    /**
     * 获取当前登录用户信息
     */
    UserVO me();

    /**
     * 修改当前用户密码
     */
    void changePassword(ChangePasswordDTO dto);

    /**
     * 退出登录
     *
     * <p>本系统为无状态 JWT，服务端无需额外动作；保留方法便于后续接入黑名单/吊销。</p>
     */
    void logout();
}

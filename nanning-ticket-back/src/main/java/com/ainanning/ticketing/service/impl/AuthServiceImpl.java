package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.security.JwtUtil;
import com.ainanning.ticketing.common.security.LoginUser;
import com.ainanning.ticketing.common.security.PasswordEncoder;
import com.ainanning.ticketing.common.security.SecurityContextHolder;
import com.ainanning.ticketing.dto.ChangePasswordDTO;
import com.ainanning.ticketing.dto.LoginDTO;
import com.ainanning.ticketing.entity.SysUser;
import com.ainanning.ticketing.mapper.SysUserMapper;
import com.ainanning.ticketing.service.AuthService;
import com.ainanning.ticketing.vo.LoginUserVO;
import com.ainanning.ticketing.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 *
 * <p>登录策略：
 * <ul>
 *   <li>密码错误时 {@code loginFailCount + 1}；连续 5 次失败 → 锁定 15 分钟</li>
 *   <li>登录成功时清零失败次数、刷新 {@code lastLoginAt / lastLoginIp}</li>
 *   <li>账号停用 → {@code LOGIN_DISABLED}；账号锁定 → {@code LOGIN_LOCKED}</li>
 * </ul>
 * </p>
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 连续失败上限 */
    private static final int MAX_FAIL_COUNT = 5;
    /** 锁定时长（分钟） */
    private static final int LOCK_MINUTES = 15;

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginUserVO login(LoginDTO dto, String clientIp) {
        log.info("[登录] username={}, ip={}", dto.getUsername(), clientIp);

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
                        .isNull(SysUser::getDeletedAt));
        if (user == null) {
            log.warn("[登录] 用户不存在 username={}", dto.getUsername());
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 1. 校验状态
        if (SysUser.STATUS_DISABLED.equals(user.getStatus())) {
            log.warn("[登录] 账号已停用 username={}", dto.getUsername());
            throw new BusinessException(ResultCode.LOGIN_DISABLED);
        }

        // 2. 校验锁定
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("[登录] 账号被锁定至 {} username={}", user.getLockedUntil(), dto.getUsername());
            throw new BusinessException(ResultCode.LOGIN_LOCKED);
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            sysUserMapper.incrementLoginFailCount(user.getId());
            int newFailCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            if (newFailCount >= MAX_FAIL_COUNT) {
                LocalDateTime until = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
                sysUserMapper.lockUntil(user.getId(), until);
                log.warn("[登录] 连续失败 {} 次，账号已锁定至 {} username={}",
                        newFailCount, until, dto.getUsername());
            } else {
                log.warn("[登录] 密码错误 username={}, failCount={}",
                        dto.getUsername(), newFailCount);
            }
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 4. 成功：重置失败次数 + 更新登录信息
        sysUserMapper.resetLoginFailAndUpdateLastLogin(user.getId(), clientIp);
        user.setLoginFailCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(clientIp);

        // 5. 签发 JWT
        String roleCodes = user.getRoleCodes() != null ? user.getRoleCodes() : user.getRole();
        JwtUtil.TokenInfo tokenInfo = jwtUtil.generate(
                user.getId(), user.getUsername(), user.getRole(), roleCodes);

        log.info("[登录] 成功 username={}, userId={}, role={}", user.getUsername(), user.getId(), user.getRole());

        return LoginUserVO.builder()
                .token(tokenInfo.token())
                .tokenExpireAt(tokenInfo.expireAt())
                .user(UserVO.from(user))
                .scenicIdList(UserVO.from(user).getScenicIdList())
                .roleList(UserVO.from(user).getRoleList())
                .build();
    }

    @Override
    public UserVO me() {
        LoginUser loginUser = SecurityContextHolder.required();
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return UserVO.from(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDTO dto) {
        LoginUser loginUser = SecurityContextHolder.required();
        log.info("[账号] 修改密码 userId={}", loginUser.getUserId());

        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 1. 校验原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            log.warn("[账号] 原密码错误 userId={}", loginUser.getUserId());
            throw new BusinessException(ResultCode.USER_PASSWORD_INVALID);
        }

        // 2. 新密码不能与原密码相同
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_SAME);
        }

        // 3. 强度校验（至少 6 位，长度范围内即可，字母数字不限）
        if (!isStrongEnough(dto.getNewPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_WEAK);
        }

        // 4. 写入新密码
        String newHash = passwordEncoder.encode(dto.getNewPassword());
        int rows = sysUserMapper.updatePasswordHash(user.getId(), newHash);
        if (rows == 0) {
            throw new BusinessException(ResultCode.USER_SAVE_FAILED);
        }
        log.info("[账号] 修改密码成功 userId={}", user.getId());
    }

    @Override
    public void logout() {
        LoginUser loginUser = SecurityContextHolder.get();
        if (loginUser != null) {
            log.info("[登出] userId={}", loginUser.getUserId());
        }
        // JWT 无状态：前端清除 token 即可；如需吊销可在此接入 Redis 黑名单
    }

    private boolean isStrongEnough(String pwd) {
        // 密码策略放宽：6-64 位即可，字母数字不限
        if (pwd == null) return false;
        int len = pwd.length();
        return len >= 6 && len <= 64;
    }
}

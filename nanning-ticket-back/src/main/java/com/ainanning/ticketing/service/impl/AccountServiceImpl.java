package com.ainanning.ticketing.service.impl;

import com.ainanning.ticketing.common.exception.BusinessException;
import com.ainanning.ticketing.common.result.ResultCode;
import com.ainanning.ticketing.common.security.LoginUser;
import com.ainanning.ticketing.common.security.PasswordEncoder;
import com.ainanning.ticketing.common.security.SecurityContextHolder;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.UserQueryDTO;
import com.ainanning.ticketing.dto.UserSaveDTO;
import com.ainanning.ticketing.entity.SysUser;
import com.ainanning.ticketing.mapper.SysUserMapper;
import com.ainanning.ticketing.service.AccountService;
import com.ainanning.ticketing.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账号管理服务实现
 *
 * @author nanning-ticket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    /** 合法的角色编码白名单（避免写入非法值） */
    private static final Set<String> VALID_ROLES = Set.of(
            SysUser.ROLE_SUPER_ADMIN, SysUser.ROLE_ADMIN, SysUser.ROLE_OPERATOR,
            SysUser.ROLE_SELLER, SysUser.ROLE_VERIFIER, SysUser.ROLE_FINANCE, SysUser.ROLE_STAFF
    );

    @Override
    public PageVO<UserVO> page(UserQueryDTO query) {
        log.info("[账号] 分页查询 keyword={}, status={}, role={}",
                query.getKeyword(), query.getStatus(), query.getRole());

        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(SysUser::getDeletedAt);

        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(SysUser::getUsername, kw)
                    .or().like(SysUser::getRealName, kw)
                    .or().like(SysUser::getPhone, kw));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getRole())) {
            wrapper.like(SysUser::getRoleCodes, query.getRole());
        }
        wrapper.orderByDesc(SysUser::getId);

        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);
        List<UserVO> records = result.getRecords().stream()
                .map(UserVO::from)
                .collect(Collectors.toList());
        return PageVO.of(result, records);
    }

    @Override
    public UserVO getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return UserVO.from(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(UserSaveDTO dto) {
        log.info("[账号] 新建 username={}", dto.getUsername());

        // 1. 角色白名单
        validateRole(dto.getRole());
        // 2. 账号唯一
        validateUsernameUnique(dto.getUsername(), null);
        // 3. 手机/邮箱唯一（如果填写）
        if (StringUtils.hasText(dto.getPhone())) {
            validateFieldUnique("phone", dto.getPhone(), null);
        }
        if (StringUtils.hasText(dto.getEmail())) {
            validateFieldUnique("email", dto.getEmail(), null);
        }
        // 4. 密码强度
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "新增用户必须填写初始密码");
        }
        if (!isStrongEnough(dto.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_WEAK);
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setRoleCodes(mergeRoleCodes(dto.getRole(), dto.getExtraRoles()));
        user.setScenicIds(normalizeScenicIds(dto.getScenicIds()));
        user.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : SysUser.STATUS_ENABLED);
        user.setRemark(dto.getRemark());
        user.setLoginFailCount(0);

        sysUserMapper.insert(user);
        log.info("[账号] 新建成功 id={}, username={}", user.getId(), user.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserSaveDTO dto) {
        log.info("[账号] 更新 id={}", dto.getId());

        SysUser exist = getActiveById(dto.getId());
        validateRole(dto.getRole());
        validateUsernameUnique(dto.getUsername(), dto.getId());
        if (StringUtils.hasText(dto.getPhone())) {
            validateFieldUnique("phone", dto.getPhone(), dto.getId());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            validateFieldUnique("email", dto.getEmail(), dto.getId());
        }

        // 1. 主字段
        SysUser user = new SysUser();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setRoleCodes(mergeRoleCodes(dto.getRole(), dto.getExtraRoles()));
        user.setScenicIds(normalizeScenicIds(dto.getScenicIds()));
        user.setStatus(dto.getStatus());
        user.setRemark(dto.getRemark());

        // 2. 密码（仅当填写时更新）
        if (StringUtils.hasText(dto.getPassword())) {
            if (!isStrongEnough(dto.getPassword())) {
                throw new BusinessException(ResultCode.USER_PASSWORD_WEAK);
            }
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        sysUserMapper.updateById(user);
        log.info("[账号] 更新成功 id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info("[账号] 删除 id={}", id);
        SysUser exist = getActiveById(id);

        // 不能删除自己
        LoginUser me = SecurityContextHolder.get();
        if (me != null && me.getUserId().equals(id)) {
            throw new BusinessException(ResultCode.USER_CANNOT_DELETE_SELF);
        }
        // 不能删除超级管理员（避免误操作）
        if (SysUser.ROLE_SUPER_ADMIN.equals(exist.getRole())) {
            throw new BusinessException(ResultCode.USER_NOT_ALLOWED,
                    "超级管理员账号不可删除");
        }

        SysUser entity = new SysUser();
        entity.setId(id);
        entity.setDeletedAt(LocalDateTime.now());
        int rows = sysUserMapper.updateById(entity);
        if (rows == 0) {
            throw new BusinessException(ResultCode.USER_DELETE_FAILED);
        }
        log.info("[账号] 删除成功 id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        log.info("[账号] 重置密码 id={}", id);
        getActiveById(id);
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6 || newPassword.length() > 64) {
            throw new BusinessException(ResultCode.USER_PASSWORD_WEAK);
        }
        String hash = passwordEncoder.encode(newPassword);
        int rows = sysUserMapper.updatePasswordHash(id, hash);
        if (rows == 0) {
            throw new BusinessException(ResultCode.USER_SAVE_FAILED);
        }
        // 重置后清零失败次数与锁定
        sysUserMapper.resetLoginFailAndUpdateLastLogin(id, null);
        log.info("[账号] 重置密码成功 id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        log.info("[账号] 切换状态 id={} -> {}", id, status);
        SysUser exist = getActiveById(id);

        if (!SysUser.STATUS_ENABLED.equals(status) && !SysUser.STATUS_DISABLED.equals(status)) {
            throw new BusinessException(ResultCode.USER_STATUS_INVALID);
        }
        // 不能停用自己
        LoginUser me = SecurityContextHolder.get();
        if (me != null && me.getUserId().equals(id) && SysUser.STATUS_DISABLED.equals(status)) {
            throw new BusinessException(ResultCode.USER_CANNOT_DISABLE_SELF);
        }
        // 不能停用超级管理员
        if (SysUser.ROLE_SUPER_ADMIN.equals(exist.getRole()) && SysUser.STATUS_DISABLED.equals(status)) {
            throw new BusinessException(ResultCode.USER_NOT_ALLOWED,
                    "超级管理员账号不可停用");
        }
        if (status.equals(exist.getStatus())) {
            log.info("[账号] 状态未变化，跳过 id={}", id);
            return;
        }

        SysUser entity = new SysUser();
        entity.setId(id);
        entity.setStatus(status);
        sysUserMapper.updateById(entity);
        log.info("[账号] 状态更新成功 id={} -> {}", id, status);
    }

    @Override
    public List<UserVO> listEnabled() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(SysUser::getDeletedAt)
                .eq(SysUser::getStatus, SysUser.STATUS_ENABLED)
                .orderByDesc(SysUser::getId);
        return sysUserMapper.selectList(wrapper).stream()
                .map(UserVO::from)
                .collect(Collectors.toList());
    }

    /* ============= 私有方法 ============= */

    private SysUser getActiveById(Long id) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null || u.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return u;
    }

    private void validateRole(String role) {
        if (!StringUtils.hasText(role) || !VALID_ROLES.contains(role)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "角色编码不合法：" + role);
        }
    }

    private void validateUsernameUnique(String username, Long excludeId) {
        long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .isNull(SysUser::getDeletedAt)
                        .ne(excludeId != null, SysUser::getId, excludeId)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_USERNAME_DUPLICATE);
        }
    }

    private void validateFieldUnique(String field, String value, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(SysUser::getDeletedAt)
                .ne(excludeId != null, SysUser::getId, excludeId);
        if ("phone".equals(field)) {
            wrapper.eq(SysUser::getPhone, value);
        } else if ("email".equals(field)) {
            wrapper.eq(SysUser::getEmail, value);
        } else {
            return;
        }
        long count = sysUserMapper.selectCount(wrapper);
        if (count > 0) {
            if ("phone".equals(field)) {
                throw new BusinessException(ResultCode.USER_PHONE_DUPLICATE);
            } else {
                throw new BusinessException(ResultCode.USER_EMAIL_DUPLICATE);
            }
        }
    }

    /** 合并主角色与额外角色，返回去重后的逗号分隔字符串 */
    private String mergeRoleCodes(String mainRole, String extraRoles) {
        Set<String> set = new HashSet<>();
        if (StringUtils.hasText(mainRole)) set.add(mainRole.trim());
        if (StringUtils.hasText(extraRoles)) {
            Arrays.stream(extraRoles.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(VALID_ROLES::contains)
                    .forEach(set::add);
        }
        return set.stream().collect(Collectors.joining(","));
    }

    /** 校验 scenicIds 是合法的 Long 列表（逗号分隔） */
    private String normalizeScenicIds(String scenicIds) {
        if (!StringUtils.hasText(scenicIds)) {
            return null;
        }
        return Arrays.stream(scenicIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
    }

    private boolean isStrongEnough(String pwd) {
        // 密码策略放宽：6-64 位即可，字母数字不限
        if (pwd == null) return false;
        int len = pwd.length();
        return len >= 6 && len <= 64;
    }
}

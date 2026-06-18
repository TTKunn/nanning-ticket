package com.ainanning.ticketing.config;

import com.ainanning.ticketing.common.security.PasswordEncoder;
import com.ainanning.ticketing.entity.SysUser;
import com.ainanning.ticketing.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 系统用户启动初始化器
 *
 * <p>应用启动时检查 {@code sys_user} 表是否为空，若为空则插入 5 个内置账号（密码已 BCrypt 哈希）。
 * 内置账号仅用于演示 / 联调；生产环境务必尽快修改默认密码并清理此处的弱口令。</p>
 *
 * <pre>
 *   admin   / 123456    - 超级管理员
 *   manager / 123456    - 管理员
 *   seller  / 123456    - 售票员（仅 1,2 号园区）
 *   verify  / 123456    - 检票员（1,2,3 号园区）
 *   finance / 123456    - 财务
 * </pre>
 *
 * @author nanning-ticket
 */
@Slf4j
@Component
@Order(0)  // 早于其他 Runner
@RequiredArgsConstructor
public class AuthBootstrap implements ApplicationRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().isNull(SysUser::getDeletedAt));
        if (count != null && count > 0) {
            log.info("[AuthBootstrap] sys_user 已存在 {} 条记录，跳过种子数据", count);
            return;
        }

        log.info("[AuthBootstrap] sys_user 为空，开始写入内置演示账号 ...");
        saveSeed("admin",   "123456", "系统管理员", "13800000000", "admin@ainanning.com",
                SysUser.ROLE_SUPER_ADMIN, "SUPER_ADMIN,ADMIN,OPERATOR", null,
                "内置超级管理员，请尽快修改默认密码");
        saveSeed("manager", "123456", "王运营",    "13800000001", "manager@ainanning.com",
                SysUser.ROLE_ADMIN,       "ADMIN,OPERATOR",                null,
                "运营管理员");
        saveSeed("seller",  "123456", "李售票",    "13800000002", "seller@ainanning.com",
                SysUser.ROLE_SELLER,      "SELLER",                        "1,2",
                "窗口售票员");
        saveSeed("verify",  "123456", "张检票",    "13800000003", "verify@ainanning.com",
                SysUser.ROLE_VERIFIER,    "VERIFIER",                      "1,2,3",
                "检票员");
        saveSeed("finance", "123456", "赵财务",    "13800000004", "finance@ainanning.com",
                SysUser.ROLE_FINANCE,     "FINANCE,ADMIN",                 null,
                "财务对账");
        log.info("[AuthBootstrap] 5 个内置账号已就绪，请尽快在生产环境修改默认密码");
    }

    private void saveSeed(String username, String rawPwd, String realName, String phone, String email,
                          String role, String roleCodes, String scenicIds, String remark) {
        SysUser u = new SysUser();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPwd));
        u.setRealName(realName);
        u.setPhone(phone);
        u.setEmail(email);
        u.setRole(role);
        u.setRoleCodes(roleCodes);
        u.setScenicIds(scenicIds);
        u.setStatus(SysUser.STATUS_ENABLED);
        u.setLoginFailCount(0);
        u.setRemark(remark);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.insert(u);
        log.info("[AuthBootstrap] 已创建账号: {}/{} ({})", username, rawPwd, realName);
    }
}

package com.ainanning.ticketing.mapper;

import com.ainanning.ticketing.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 系统用户 Mapper
 *
 * @author nanning-ticket
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 累加登录失败次数（+1）
     */
    @Update("UPDATE sys_user SET login_fail_count = login_fail_count + 1, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int incrementLoginFailCount(@Param("id") Long id);

    /**
     * 重置登录失败次数并更新最近登录信息
     */
    @Update("UPDATE sys_user SET login_fail_count = 0, locked_until = NULL, " +
            "last_login_at = NOW(), last_login_ip = #{ip}, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int resetLoginFailAndUpdateLastLogin(@Param("id") Long id, @Param("ip") String ip);

    /**
     * 设置账号锁定截止时间
     */
    @Update("UPDATE sys_user SET locked_until = #{until}, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int lockUntil(@Param("id") Long id, @Param("until") java.time.LocalDateTime until);

    /**
     * 更新密码哈希
     */
    @Update("UPDATE sys_user SET password_hash = #{hash}, updated_at = NOW() " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int updatePasswordHash(@Param("id") Long id, @Param("hash") String hash);
}

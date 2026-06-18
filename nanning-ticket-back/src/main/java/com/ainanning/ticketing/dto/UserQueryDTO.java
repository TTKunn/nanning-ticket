package com.ainanning.ticketing.dto;

import com.ainanning.ticketing.common.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询参数
 *
 * @author nanning-ticket
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询参数")
public class UserQueryDTO extends PageQuery {

    @Schema(description = "关键字（登录账号 / 姓名 / 手机号 模糊搜索）")
    private String keyword;

    @Schema(description = "状态：启用/停用")
    private String status;

    @Schema(description = "角色编码过滤")
    private String role;
}

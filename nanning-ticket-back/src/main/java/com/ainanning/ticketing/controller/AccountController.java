package com.ainanning.ticketing.controller;

import com.ainanning.ticketing.common.result.Result;
import com.ainanning.ticketing.common.security.RequireRoles;
import com.ainanning.ticketing.common.vo.PageVO;
import com.ainanning.ticketing.dto.UserQueryDTO;
import com.ainanning.ticketing.dto.UserSaveDTO;
import com.ainanning.ticketing.service.AccountService;
import com.ainanning.ticketing.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 账号管理 Controller
 *
 * <p>路由前缀：/api/accounts
 * <br>权限：除"查看下拉"外，全部限定为 SUPER_ADMIN / ADMIN。</p>
 *
 * @author nanning-ticket
 */
@Tag(name = "00b. 账号管理", description = "后台用户的增删改查、启停与重置密码")
@Validated
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @RequireRoles({"SUPER_ADMIN", "ADMIN"})
    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<PageVO<UserVO>> page(@Valid UserQueryDTO query) {
        return Result.success(accountService.page(query));
    }

    @RequireRoles({"SUPER_ADMIN", "ADMIN"})
    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getById(
            @Parameter(description = "用户 ID") @PathVariable Long id) {
        return Result.success(accountService.getById(id));
    }

    @RequireRoles({"SUPER_ADMIN", "ADMIN"})
    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserSaveDTO dto) {
        return Result.success("新建成功", accountService.create(dto));
    }

    @RequireRoles({"SUPER_ADMIN", "ADMIN"})
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "用户 ID") @PathVariable Long id,
            @Valid @RequestBody UserSaveDTO dto) {
        dto.setId(id);
        accountService.update(dto);
        return Result.success("更新成功", null);
    }

    @RequireRoles({"SUPER_ADMIN", "ADMIN"})
    @Operation(summary = "删除用户（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "用户 ID") @PathVariable Long id) {
        accountService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @RequireRoles({"SUPER_ADMIN", "ADMIN"})
    @Operation(summary = "切换状态：启用/停用")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "用户 ID") @PathVariable Long id,
            @Parameter(description = "状态：启用/停用") @RequestParam String status) {
        accountService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }

    @RequireRoles({"SUPER_ADMIN", "ADMIN"})
    @Operation(summary = "重置用户密码")
    @PatchMapping("/{id}/password")
    public Result<Void> resetPassword(
            @Parameter(description = "用户 ID") @PathVariable Long id,
            @Parameter(description = "新密码（明文，至少 8 位含字母与数字）")
            @RequestParam String newPassword) {
        accountService.resetPassword(id, newPassword);
        return Result.success("密码已重置", null);
    }

    @Operation(summary = "获取启用的用户下拉列表（无需管理员权限）")
    @GetMapping("/options")
    public Result<List<UserVO>> listEnabled() {
        return Result.success(accountService.listEnabled());
    }
}

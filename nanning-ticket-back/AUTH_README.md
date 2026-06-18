# 登录 / 账号 / 鉴权 模块使用说明

本模块为系统引入完整的登录鉴权与账号管理能力，包含 JWT 签发、基于拦截器的访问控制、角色权限、账号 CRUD、密码管理与登录失败锁定。

---

## 一、运行准备

### 1. 引入数据库表

执行 `sql/migration_auth.sql` 创建 `sys_user` 表：

```bash
mysql -h 47.109.151.103 -u nanning_ticket -p nanning_ticket < sql/migration_auth.sql
```

### 2. 启动后端

应用启动时 `AuthBootstrap` 会检查 `sys_user` 表是否为空，**自动**写入 5 个内置演示账号（密码统一为 `123456`）：

| 账号 | 密码 | 角色 | 管辖园区 |
|------|------|------|----------|
| admin | 123456 | SUPER_ADMIN | 全部 |
| manager | 123456 | ADMIN | 全部 |
| seller | 123456 | SELLER | 1,2 |
| verify | 123456 | VERIFIER | 1,2,3 |
| finance | 123456 | FINANCE | 全部 |

> ⚠️ 生产环境务必在首次登录后通过"修改密码"或账号管理页修改默认密码。

### 3. 启动前端

```bash
cd nanning-ticket-front
npm install
npm run dev
```

浏览器访问 `http://localhost:5173/`，未登录会自动跳到 `/login`。

---

## 二、关键设计

### 1. 自研轻量级安全栈

- **不依赖 Spring Security**：本项目保持"轻"的设计风格，安全相关代码集中在 `common.security` 包内。
- **JWT (HS256)**：使用 jjwt 0.12.5 签发与解析。
- **BCrypt 哈希**：使用 at.favre.lib:bcrypt 0.10.2（强度 10）。
- **ThreadLocal 上下文**：`SecurityContextHolder` 在请求线程内传递 `LoginUser`，业务代码可直接 `SecurityContextHolder.required()` 获取。

### 2. 拦截器

`AuthInterceptor` 注册到 `/api/**`，**排除**：
- `POST /api/auth/login`（登录本身）
- `POST /api/auth/captcha`（图形验证码，预留）
- Swagger 相关路径
- 错误页

业务 Controller 只需在方法上标注 `@RequireRoles({"SUPER_ADMIN", "ADMIN"})` 即可限定角色访问。超级管理员 (`SUPER_ADMIN`) 始终放行。

公开接口用 `@PublicEndpoint` 标记。

### 3. 登录失败策略

- 连续 5 次密码错误 → 锁定账号 15 分钟。
- 锁定期间调用 `/api/auth/login` 返回 `LOGIN_LOCKED`。
- 登录成功 → 重置失败计数 + 记录 `lastLoginAt / lastLoginIp`。

### 4. 密码强度

- 长度 6-64 位
- 字母与数字都允许，不强制组合
- 演示账号与初始化密码长度通常为 8 位，提示用户登录后修改

---

## 三、API 列表

### 认证

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 公开 | 登录，返回 token + user + 角色 + 管辖园区 |
| POST | `/api/auth/logout` | 登录 | 登出（前端清 token，服务端无状态） |
| GET  | `/api/auth/me` | 登录 | 当前用户信息 |
| PUT  | `/api/auth/password` | 登录 | 修改自己的密码 |

### 账号管理

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET    | `/api/accounts?keyword=&status=&role=&pageNum=1&pageSize=10` | SUPER_ADMIN / ADMIN | 分页查询 |
| GET    | `/api/accounts/{id}` | SUPER_ADMIN / ADMIN | 详情 |
| POST   | `/api/accounts` | SUPER_ADMIN / ADMIN | 新增 |
| PUT    | `/api/accounts/{id}` | SUPER_ADMIN / ADMIN | 更新（password 为空=不修改） |
| DELETE | `/api/accounts/{id}` | SUPER_ADMIN / ADMIN | 软删除（不能删自己 / 超管） |
| PATCH  | `/api/accounts/{id}/status?status=启用/停用` | SUPER_ADMIN / ADMIN | 启停（不能停自己 / 超管） |
| PATCH  | `/api/accounts/{id}/password?newPassword=xxx` | SUPER_ADMIN / ADMIN | 重置密码 |
| GET    | `/api/accounts/options` | 登录 | 启用的用户下拉列表 |

### 登录请求 / 响应

**请求：**
```json
{
  "username": "admin",
  "password": "123456"
}
```

**成功响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenExpireAt": 1718620800000,
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "phone": "13800000000",
      "email": "admin@ainanning.com",
      "role": "SUPER_ADMIN",
      "roleList": ["SUPER_ADMIN", "ADMIN", "OPERATOR"],
      "scenicIdList": null,
      "status": "启用",
      "lastLoginAt": "2026-06-17T10:00:00"
    },
    "scenicIdList": null,
    "roleList": ["SUPER_ADMIN", "ADMIN", "OPERATOR"]
  }
}
```

---

## 四、前端集成

### 状态管理

`src/store/user.js` 是基于 `reactive` 的单例 store，提供：

```js
import { userStore } from '@/store/user'

userStore.isLoggedIn           // 是否已登录
userStore.isSuperAdmin         // 是否超管
userStore.hasAnyRole([...])    // 校验角色
userStore.canAccessScenic(id)  // 校验园区
userStore.setSession(payload)  // 登录后写入
userStore.clear()              // 登出
```

### 路由守卫

- 未登录访问 `/#/xxx` → 自动跳 `/#/login`，并把目标路径记到 `sessionStorage.loginNext`。
- 登录成功后跳回原路径或首页。
- 401 / 32xx → 清空会话并跳登录。

### 修改密码

顶部右侧用户菜单 → 修改密码 → 弹窗内填写原密码 + 新密码（6-64 位字母数字均可） → 成功后提示重新登录。

---

## 五、扩展指引

### 新增自定义角色

1. 在 `SysUser` 的 `ROLE_*` 常量加一个；
2. 在 `AccountServiceImpl.VALID_ROLES` 加进白名单；
3. 在前端 `PageAccounts.vue` 的 `extraRoleOptions` 加一项；
4. 在 `store/user.js` 的 `ROLE_LABELS` 加中文标签。

### 接入 SSO / OAuth2

把 `AuthServiceImpl.login` 改为基于第三方回调的逻辑；JWT 签发与拦截器流程无需改动。

### Token 吊销（黑名单）

把 `AuthServiceImpl.logout` 改为把当前 jti 写入 Redis（带 TTL），`AuthInterceptor` 在解析后查询 Redis 黑名单。

### 图形验证码

启用 `AuthController` 预留的 `/api/auth/captcha`，使用 Hutool / Kaptcha 之类的工具生成 base64 图片 + 缓存 key 到 Redis，前端在登录时一并提交。

# 前端接入真实后端 + 跨域（CORS）配置说明

> 本次改造已在前端使用 **Axios** + **Vite 代理**调用后端接口，
> 并新增了全局 **Loading** 与 **错误弹窗**。本文档说明跨域问题的两种解法和上线要点。

---

## 一、已完成的改动

### 1. 前端
| 模块 | 文件 | 说明 |
|---|---|---|
| 网络库 | `package.json` | 新增 `axios ^1.18.0` |
| 构建配置 | `vite.config.js` | 配置 `/api` 代理到 `http://localhost:8090` |
| 请求封装 | `src/api/request.js` | Axios 实例 + 拦截器，统一处理 `Result<T>`、Loading、错误弹窗 |
| 错误提示 | `src/components/ui/Message.js` | 顶部轻量消息弹窗，复用项目色板（与 `tag` 风格一致） |
| 加载遮罩 | `src/components/ui/Loading.js` | 全屏遮罩 + 旋转图标 |
| API 模块 | `src/api/{scenic,rule,ticket,inventory,sale,verify,order,voucher,channel,report,setting}.js` | 按后端接口拆分 |
| 页面 | `src/components/Page*.vue` | 12 个页面已全部接入真实接口，含 Loading / 空状态 / 分页 / 弹窗 |

### 2. 后端
| 文件 | 说明 |
|---|---|
| `nanning-ticket-back/src/main/java/com/ainanning/ticketing/config/CorsConfig.java` | 新增全局 CORS 过滤器（可选，使用 Vite 代理时不依赖此配置） |

---

## 二、跨域问题（CORS）配置

### 方案 A：开发期推荐 —— Vite 代理（同源）
已写死在 `nanning-ticket-front/vite.config.js`：

```js
server: {
  host: '0.0.0.0',
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8090',
      changeOrigin: true,
    },
  },
},
```

- 浏览器始终访问 `http://localhost:5173/api/...`，由 Vite 内部转发 → **不触发 CORS**
- 后端无需任何改动
- 启动方式：
  ```bash
  # 1) 启动后端
  cd nanning-ticket-back
  mvn spring-boot:run

  # 2) 启动前端（新开终端）
  cd nanning-ticket-front
  npm install
  npm run dev
  ```

### 方案 B：生产环境 —— 后端开启 CORS
当把前端打包后部署到独立域名（例如 `https://admin.ainanning.com`），
后端必须显式允许该来源的跨域请求。

**已提供的 `CorsConfig.java` 默认配置：**
```java
config.addAllowedOriginPattern("*"); // 生产请改为具体域名
config.addAllowedHeader("*");
config.addAllowedMethod("*");
config.setAllowCredentials(true);
```

**生产建议改为具体域名：**
```java
config.addAllowedOrigin("https://admin.ainanning.com");
config.addAllowedOrigin("https://www.ainanning.com");
```

如果使用 Nginx 反向代理，也可以直接在 Nginx 一层加：
```nginx
location /api/ {
    proxy_pass http://localhost:8090/api/;
    add_header 'Access-Control-Allow-Origin' '$http_origin' always;
    add_header 'Access-Control-Allow-Credentials' 'true' always;
    add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, PATCH, DELETE, OPTIONS' always;
    add_header 'Access-Control-Allow-Headers' 'Authorization, Content-Type' always;
    if ($request_method = 'OPTIONS') { return 204; }
}
```

---

## 三、Loading & 错误提示的使用

### 1. Loading —— 全自动
`src/api/request.js` 的请求/响应拦截器已经自动触发：
- **多个并发请求**使用计数器管理，只有最后一个返回才关闭遮罩
- 单个请求可显式关闭：`request({ url, method, silence: true })`

### 2. 错误提示 —— 全自动
- **业务异常**：`Result.code !== 200` → 顶部红色弹窗显示后端 `message`
- **网络异常**：超时、跨域、5xx → 自动分类（401/403/404/500/超时）并弹窗
- 单个请求可显式关闭：`request({ url, method, silence: true })`

### 3. 页面级 Loading
页面内首次加载表格时显示 "加载中..."，沿用 `empty-state` 样式：
```vue
<tr v-if="loading"><td colspan="9" class="empty-state">加载中...</td></tr>
<tr v-else-if="!list.length"><td colspan="9" class="empty-state">暂无数据</td></tr>
```

---

## 四、待你确认的差异

- 旧的 `Mock` 数据中包含部分字段（如 `id` 为 `'TK001'` 的字符串），
  接入真实接口后 `id` 改为后端返回的数字 `Long`。
- 旧的 `status` 中文枚举（"上架中"/"已下架"、"运营中"/"暂停运营"）
  已对齐后端的 `在售/停售`、`启用/禁用` 等字面值。
- 旧的"项目规则配置"页里"票种分组/收费对象/核销方式"等非核心字段
  因后端未提供这些列，已删除；保留 `类型/优先级/有效期/状态` 等核心字段。
- 后端目前未提供"账号管理"接口，"系统设置 → 账号管理"页保留 UI 占位。

---

## 五、启动校验清单

- [ ] MySQL 已就绪（`sql/init.sql` 已执行）
- [ ] `nanning-ticket-back` 启动成功，访问 `http://localhost:8090/swagger-ui.html` 能看到接口
- [ ] `nanning-ticket-front` `npm install` 已完成（含 `axios`）
- [ ] `npm run dev` 启动后访问 `http://localhost:5173`
- [ ] 切换到任一业务页，控制台 Network 面板能看到 `/api/...` 请求且 200 成功
- [ ] 故意关掉后端，刷新页面 → 顶部弹窗提示"无法连接到服务器"

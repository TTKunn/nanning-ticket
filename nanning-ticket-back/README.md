# AI 南宁票务管理系统 - 后端

> 基于 Spring Boot 3.2 + MyBatis-Plus 3.5 + Java 17 构建的票务管理后台 API。

## 一、技术栈

| 类别       | 技术                            | 版本        |
|------------|--------------------------------|-------------|
| 基础框架   | Spring Boot                    | 3.2.5       |
| 持久层     | MyBatis-Plus                   | 3.5.5       |
| 数据库     | MySQL                          | 8.0+        |
| Java       | OpenJDK                        | 17          |
| 构建工具   | Maven                          | 3.8+        |
| API 文档   | springdoc-openapi              | 2.3.0       |
| 工具库     | Lombok                         | 最新        |

## 二、模块进度

- [x] **〇** 通用基础（Result / 异常 / 分页 / MyBatis-Plus 配置）
- [x] **一** 园区管理（Scenic）
- [x] **二** 项目规则配置（Rule）
- [x] **三** 票种管理（Ticket）
- [x] **四** 库存管理（Inventory）
- [x] **五** 窗口售票（Sales）
- [x] **六** 检票（Verify）
- [x] **七** 票据管理（Voucher）✅
- [x] **八** 订单管理（Order）✅
- [x] **九** 渠道管理（Channel）✅
- [x] **十** 数据报表（Report）✅
- [x] **十一** 系统设置（Setting）✅

## 三、项目结构

```
nanning-ticket-back
├── pom.xml
├── README.md
├── .gitignore
├── sql
│   └── init.sql                 # 数据库初始化脚本
└── src
    └── main
        ├── java/com/ainanning/ticketing
        │   ├── NanningTicketApplication.java    # 启动类
        │   ├── common                           # 通用基础
        │   │   ├── exception
        │   │   ├── query
        │   │   ├── result
        │   │   └── vo
        │   ├── config                           # 配置类
        │   │   └── MybatisPlusConfig.java
        │   ├── entity                           # 实体层
        │   │   ├── BaseEntity.java
        │   │   ├── Scenic.java
        │   │   ├── Rule.java
        │   │   ├── Ticket.java
        │   │   ├── Inventory.java
        │   │   ├── Sale.java
        │   │   ├── SaleItem.java
        │   │   ├── VerifyRecord.java
        │   │   ├── Voucher.java
        │   │   ├── Order.java
        │   │   ├── OrderItem.java
        │   │   ├── Channel.java
        │   │   ├── ChannelSettlement.java
        │   │   ├── （报表模块不建新实体）
        │   │   ├── Setting.java
        │   │   └── OpLog.java
        │   ├── mapper                           # DAO 层
        │   │   ├── ScenicMapper.java
        │   │   ├── RuleMapper.java
        │   │   ├── TicketMapper.java
        │   │   ├── InventoryMapper.java
        │   │   ├── SaleMapper.java
        │   │   ├── SaleItemMapper.java
        │   │   ├── VerifyRecordMapper.java
        │   │   ├── VoucherMapper.java
        │   │   ├── OrderMapper.java
        │   │   ├── OrderItemMapper.java
        │   │   ├── ChannelMapper.java
        │   │   ├── ChannelSettlementMapper.java
        │   │   ├── SettingMapper.java
        │   │   └── OpLogMapper.java
        │   ├── dto                              # 请求 DTO
        │   │   ├── ScenicQueryDTO.java
        │   │   ├── ScenicSaveDTO.java
        │   │   ├── RuleQueryDTO.java
        │   │   ├── RuleSaveDTO.java
        │   │   ├── TicketQueryDTO.java
        │   │   ├── TicketSaveDTO.java
        │   │   ├── InventoryQueryDTO.java
        │   │   ├── InventorySaveDTO.java
        │   │   ├── InventoryBatchDTO.java
        │   │   ├── SaleQueryDTO.java
        │   │   ├── SaleCreateDTO.java
        │   │   ├── SaleItemCreateDTO.java
        │   │   ├── SaleRefundDTO.java
        │   │   ├── VerifyQueryDTO.java
        │   │   ├── VerifyRequestDTO.java
        │   │   ├── VoucherQueryDTO.java
        │   │   ├── VoucherRevokeDTO.java
        │   │   ├── VoucherReissueDTO.java
        │   │   ├── VoucherMarkPrintedDTO.java
        │   │   ├── OrderQueryDTO.java
        │   │   ├── OrderCreateDTO.java
        │   │   ├── OrderItemCreateDTO.java
        │   │   ├── OrderPayDTO.java
        │   │   ├── OrderCancelDTO.java
        │   │   ├── OrderRefundDTO.java
        │   │   ├── ChannelQueryDTO.java
        │   │   ├── ChannelSaveDTO.java
        │   │   ├── ChannelStatusDTO.java
        │   │   ├── ChannelCommissionDTO.java
        │   │   ├── SettlementCreateDTO.java
        │   │   ├── SettlementQueryDTO.java
        │   │   ├── SettlementActionDTO.java
        │   │   ├── ReportQueryDTO.java
        │   │   ├── SettingQueryDTO.java
        │   │   ├── SettingSaveDTO.java
        │   │   ├── OpLogQueryDTO.java
        │   │   └── OpLogRecordDTO.java
        │   ├── vo                               # 响应 VO
        │   │   ├── ScenicVO.java
        │   │   ├── ScenicOptionVO.java
        │   │   ├── RuleVO.java
        │   │   ├── RuleOptionVO.java
        │   │   ├── TicketVO.java
        │   │   ├── TicketOptionVO.java
        │   │   ├── InventoryVO.java
        │   │   ├── SaleVO.java
        │   │   ├── SaleItemVO.java
        │   │   ├── VerifyRecordVO.java
        │   │   ├── VerifyResultVO.java
        │   │   ├── VoucherVO.java
        │   │   ├── VoucherStatsVO.java
        │   │   ├── OrderVO.java
        │   │   ├── OrderItemVO.java
        │   │   ├── OrderStatsVO.java
        │   │   ├── ChannelVO.java
        │   │   ├── ChannelOptionVO.java
        │   │   ├── ChannelStatsVO.java
        │   │   ├── ChannelSettlementVO.java
        │   │   ├── ReportOverviewVO.java
        │   │   ├── ReportTrendVO.java
        │   │   ├── ReportRankingVO.java
        │   │   ├── ReportVisitFunnelVO.java
        │   │   ├── ReportInventoryVO.java
        │   │   ├── ReportPaymentVO.java
        │   │   ├── SettingVO.java
        │   │   └── OpLogVO.java
        │   ├── service                          # 业务层
        │   │   ├── ScenicService.java
        │   │   ├── RuleService.java
        │   │   ├── TicketService.java
        │   │   ├── InventoryService.java
        │   │   ├── SaleService.java
        │   │   ├── VerifyService.java
        │   │   ├── VoucherService.java
        │   │   ├── OrderService.java
        │   │   ├── ChannelService.java
        │   │   ├── ChannelSettlementService.java
        │   │   ├── ReportService.java
        │   │   ├── SettingService.java
        │   │   ├── OpLogService.java
        │   │   └── impl
        │   │       ├── ScenicServiceImpl.java
        │   │       ├── RuleServiceImpl.java
        │   │       ├── TicketServiceImpl.java
        │   │       ├── InventoryServiceImpl.java
        │   │       ├── SaleServiceImpl.java
        │   │       ├── VerifyServiceImpl.java
        │   │       ├── VoucherServiceImpl.java
        │   │       ├── OrderServiceImpl.java
        │   │       ├── ChannelServiceImpl.java
        │   │       ├── ChannelSettlementServiceImpl.java
        │   │       ├── ReportServiceImpl.java
        │   │       ├── SettingServiceImpl.java
        │   │       └── OpLogServiceImpl.java
        │   └── controller                       # 控制器层
        │       ├── ScenicController.java
        │       ├── RuleController.java
        │       ├── TicketController.java
        │       ├── InventoryController.java
        │       ├── SaleController.java
        │       ├── VerifyController.java
        │       ├── VoucherController.java
        │       ├── OrderController.java
        │       ├── ChannelController.java
        │       ├── ChannelSettlementController.java
        │       ├── ReportController.java
        │       ├── SettingController.java
        │       └── OpLogController.java
        └── resources
            └── application.yml
```

## 四、快速开始

### 1. 准备数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本（先建库，再执行表结构）
mysql> CREATE DATABASE nanning_ticket DEFAULT CHARSET utf8mb4;
mysql> USE nanning_ticket;
mysql> SOURCE sql/init.sql;
```

### 2. 修改配置

打开 `src/main/resources/application.yml`，根据本机环境修改：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/nanning_ticket?...
    username: root
    password: root   # ← 改成你的密码
```

### 3. 启动项目

```bash
# 方式一：Maven
mvn spring-boot:run

# 方式二：IDE
# 直接运行 NanningTicketApplication.main()
```

### 4. 访问接口

| 地址                                                | 说明          |
|----------------------------------------------------|---------------|
| http://localhost:8090/swagger-ui.html              | Swagger UI    |
| http://localhost:8090/v3/api-docs                  | OpenAPI JSON  |

## 五、接口示例

### 园区分页查询

```http
GET /api/scenics?keyword=青秀山&status=运营中&pageNum=1&pageSize=10
```

### 园区详情

```http
GET /api/scenics/1
```

### 新建园区

```http
POST /api/scenics
Content-Type: application/json

{
  "name": "青秀山风景区",
  "icon": "山",
  "iconBg": "#f0fdf4",
  "level": "国家5A级景区",
  "address": "南宁市青秀区青秀山路",
  "openTime": "06:00-22:00",
  "description": "青秀山是南宁的标志性景区",
  "status": "运营中",
  "sort": 100
}
```

### 切换园区状态

```http
PATCH /api/scenics/1/status?status=暂停运营
```

### 规则分页查询（按园区）

```http
GET /api/rules?scenicId=1&type=折扣&status=启用&pageNum=1&pageSize=10
```

### 新建规则

```http
POST /api/rules
Content-Type: application/json

{
  "scenicId": 1,
  "name": "学生票 9 折",
  "code": "STUDENT_DISCOUNT",
  "type": "折扣",
  "description": "全日制学生凭学生证享 9 折优惠",
  "config": "{\"rate\":0.9,\"needStudentCard\":true}",
  "priority": 95,
  "status": "启用",
  "effectiveFrom": "2026-01-01",
  "effectiveTo": "2026-12-31"
}
```

### 切换规则状态

```http
PATCH /api/rules/1/status?status=禁用
```

### 规则下拉选项（按园区过滤启用项）

```http
GET /api/rules/options?scenicId=1
```

### 票种分页查询（按园区）

```http
GET /api/tickets?scenicId=1&category=单票&status=在售&pageNum=1&pageSize=10
```

### 新建票种

```http
POST /api/tickets
Content-Type: application/json

{
  "scenicId": 1,
  "name": "青秀山儿童票",
  "code": "QXS_CHILD",
  "category": "单票",
  "price": 30.00,
  "costPrice": 20.00,
  "description": "身高 1.2-1.4 米儿童",
  "tags": ["学生", "推荐"],
  "validDays": 1,
  "refundable": true,
  "ruleIds": [1, 3],
  "status": "在售",
  "sort": 90
}
```

### 上架/下架票种

```http
PATCH /api/tickets/1/status?status=停售
```

### 票种下拉选项（按园区过滤在售项）

```http
GET /api/tickets/options?scenicId=1
```

### 库存分页查询（按票种 / 日期范围）

```http
GET /api/inventories?ticketId=1&dateFrom=2026-06-14&dateTo=2026-06-20&pageSize=31
```

### 新建单日库存

```http
POST /api/inventories
Content-Type: application/json

{
  "ticketId": 1,
  "inventoryDate": "2026-06-21",
  "total": 500,
  "status": "开放",
  "remark": "周末"
}
```

### 按日期范围批量创建

```http
POST /api/inventories/batch
Content-Type: application/json

{
  "ticketId": 1,
  "startDate": "2026-07-01",
  "endDate": "2026-07-31",
  "total": 500,
  "remark": "七月库存"
}
```

### 关闭库存

```http
PATCH /api/inventories/1/status?status=关闭
```

### 销售分页查询（按园区 / 状态 / 日期范围）

```http
GET /api/sales?scenicId=1&status=已支付&dateFrom=2026-06-14&dateTo=2026-06-20&pageNum=1&pageSize=10
```

### 销售详情（含明细）

```http
GET /api/sales/1
```

### 窗口售票（创建销售）

```http
POST /api/sales
Content-Type: application/json

{
  "scenicId": 1,
  "windowName": "1号窗口",
  "salespersonName": "李华",
  "visitorName": "王芳",
  "visitorPhone": "13800000001",
  "paymentMethod": "微信",
  "remark": "现场购票",
  "items": [
    { "ticketId": 1, "inventoryDate": "2026-06-20", "quantity": 2 },
    { "ticketId": 3, "inventoryDate": "2026-06-20", "quantity": 1 }
  ]
}
```

### 整单退票（items 为空）

```http
POST /api/sales/1/refund
Content-Type: application/json

{
  "reason": "客户取消"
}
```

### 部分退票（按明细）

```http
POST /api/sales/1/refund
Content-Type: application/json

{
  "reason": "退其中 1 张",
  "items": [
    { "saleItemId": 1, "quantity": 1 }
  ]
}
```

### 取消销售单

```http
POST /api/sales/1/cancel?reason=客户改期
```

### 检票（闸机/终端核心接口）

```http
POST /api/verifies
Content-Type: application/json

{
  "voucherCode": "V202606140001",
  "verifyMethod": "扫码",
  "verifyStaffName": "张检",
  "deviceName": "1号闸机"
}
```

返回（成功）：

```json
{
  "code": 200,
  "data": {
    "result": "成功",
    "voucherCode": "V202606140001",
    "saleNo": "S202606140001",
    "ticketName": "青秀山成人票",
    "scenicName": "青秀山风景区",
    "inventoryDate": "2026-06-14",
    "unitPrice": 60.00,
    "visitorName": "王芳",
    "verifyTime": "2026-06-14 09:30:00"
  }
}
```

返回（失败示例：已使用）：

```json
{
  "code": 200,
  "data": {
    "result": "失败",
    "failReason": "已使用",
    "voucherCode": "V202606140001"
  }
}
```

### 按票据码查询检票历史

```http
GET /api/verifies/by-code?voucherCode=V202606140001
```

### 园区当日核销统计

```http
GET /api/verifies/today-stats?scenicId=1
```

### 票据分页查询（按园区 / 票种 / 状态）

```http
GET /api/vouchers?scenicId=1&status=待使用&dateFrom=2026-06-14&dateTo=2026-06-20&pageNum=1&pageSize=10
```

### 按票据码查询（扫码反查）

```http
GET /api/vouchers/by-code?voucherCode=V202606140001
```

### 按销售单查全部票据

```http
GET /api/vouchers/by-sale?saleId=1
```

### 批量作废票据（管理端，区别于退票）

```http
POST /api/vouchers/revoke
Content-Type: application/json

{
  "ids": [2, 5],
  "reason": "二维码污损",
  "staffName": "管理员A"
}
```

### 批量补发（针对已退/已作废的票）

```http
POST /api/vouchers/reissue
Content-Type: application/json

{
  "sourceIds": [4],
  "reason": "客户票丢失",
  "staffName": "管理员A"
}
```

返回：

```json
{
  "code": 200,
  "data": [
    {
      "id": 7,
      "voucherCode": "V202606141530100012",
      "status": "待使用",
      "saleId": 3,
      "ticketName": "南湖夜跑纪念票",
      "scenicName": "南湖公园",
      "validFrom": "2026-06-14",
      "validTo": "2026-06-14",
      "printCount": 0,
      "remark": "补发自原票据 V202606140004 - 客户票丢失"
    }
  ]
}
```

### 标记打印（重打纸质凭据）

```http
POST /api/vouchers/mark-printed
Content-Type: application/json

{
  "ids": [2, 3]
}
```

### 票据状态统计（园区/票种/销售单三维度）

```http
GET /api/vouchers/stats?scenicId=1
```

返回：

```json
{
  "code": 200,
  "data": {
    "scenicId": 1,
    "unusedCount": 1,
    "usedCount": 2,
    "refundCount": 0,
    "revokedCount": 0,
    "totalCount": 3,
    "usageRate": 66.67
  }
}
```

### 在线订单分页查询（按园区 / 渠道 / 状态）

```http
GET /api/orders?scenicId=1&status=已出票&channelCode=小程序&pageNum=1&pageSize=10
```

### 创建订单（创建即支付，一气呵成）

```http
POST /api/orders
Content-Type: application/json

{
  "scenicId": 1,
  "channelCode": "小程序",
  "channelName": "微信小程序",
  "userName": "小王",
  "contactName": "小王",
  "contactPhone": "13900000001",
  "payMethod": "微信",
  "items": [
    { "ticketId": 1, "inventoryDate": "2026-06-20", "quantity": 2 }
  ]
}
```

### 取消订单（仅"待支付"）

```http
POST /api/orders/1/cancel
Content-Type: application/json

{ "reason": "改主意了" }
```

### 全单退款（仅"已出票"且 voucher 全部未使用）

```http
POST /api/orders/2/refund
Content-Type: application/json

{ "reason": "客户取消" }
```

### 订单状态统计

```http
GET /api/orders/stats?scenicId=1
```

返回：

```json
{
  "code": 200,
  "data": {
    "scenicId": 1,
    "pendingCount": 1,
    "fulfilledCount": 1,
    "cancelledCount": 0,
    "refundingCount": 0,
    "refundedCount": 1,
    "partialCount": 0,
    "totalCount": 3,
    "gmvAmount": 147.90,
    "refundAmount": 19.90,
    "fulfillRate": 33.33
  }
}
```

### 渠道分页查询

```http
GET /api/channels?keyword=携程&channelType=OTA&status=启用&pageNum=1&pageSize=10
```

### 渠道下拉选项（仅启用项）

```http
GET /api/channels/options
```

### 新建渠道

```http
POST /api/channels
Content-Type: application/json

{
  "channelCode": "美团",
  "channelName": "美团旅行",
  "channelType": "OTA",
  "icon": "🟡",
  "iconBg": "#fbbf24",
  "commissionRate": 10.00,
  "contactName": "孙经理",
  "contactPhone": "13800000020",
  "settleAccount": "美团网有限公司",
  "settleBank": "招商银行北京分行",
  "settleAccountNo": "6225880000000000",
  "apiKey": "MEITUAN-LIVE-KEY-XXXX",
  "apiEndpoint": "https://open.meituan.com",
  "status": "启用",
  "sort": 110
}
```

### 调整佣金比例

```http
PATCH /api/channels/1/commission
Content-Type: application/json

{
  "commissionRate": 13.00,
  "reason": "OTA 大促谈判"
}
```

### 渠道维度统计

```http
GET /api/channels/stats
```

返回：

```json
{
  "code": 200,
  "data": {
    "totalCount": 6,
    "enabledCount": 5,
    "disabledCount": 1,
    "totalGmv": 0.00,
    "totalOrderCount": 0,
    "channelList": [
      {
        "channelId": 1,
        "channelCode": "OTA",
        "channelName": "携程旅行",
        "channelType": "OTA",
        "commissionRate": 12.00,
        "status": "启用",
        "orderCount": 0,
        "totalGmv": 0.00
      }
    ]
  }
}
```

### 生成结算单（按渠道 + 周期聚合 order 表）

```http
POST /api/channel-settlements
Content-Type: application/json

{
  "channelId": 1,
  "periodStart": "2026-06-01",
  "periodEnd": "2026-06-30",
  "remark": "6 月份携程对账"
}
```

返回：

```json
{ "code": 200, "data": 3 }
```

### 结算单分页查询

```http
GET /api/channel-settlements?channelId=1&status=待确认&periodFrom=2026-06-01&pageNum=1&pageSize=10
```

### 确认结算单（待确认 → 已确认）

```http
POST /api/channel-settlements/3/confirm
Content-Type: application/json

{ "confirmStaff": "财务小李" }
```

### 打款（已确认 → 已打款）

```http
POST /api/channel-settlements/3/pay
Content-Type: application/json

{
  "paidAmount": 0.00,
  "payTransaction": "BANK20260614001"
}
```

### 报表核心指标（Dashboard 顶部）

```http
GET /api/reports/overview?dateFrom=2026-06-01&dateTo=2026-06-14&scenicId=1
```

返回（节选）：

```json
{
  "code": 200,
  "data": {
    "dateFrom": "2026-06-01",
    "dateTo": "2026-06-14",
    "scenicId": 1,
    "saleCount": 3,
    "orderCount": 1,
    "totalTicketCount": 4,
    "saleGmv": 184.90,
    "orderGmv": 19.90,
    "totalGmv": 204.80,
    "refundAmount": 19.90,
    "netRevenue": 184.90,
    "voucherIssued": 8,
    "voucherUsed": 2,
    "voucherRefunded": 1,
    "voucherRevoked": 0,
    "useRate": 25.00,
    "inventoryTotal": 1500,
    "inventorySold": 7,
    "inventorySellRate": 0.47
  }
}
```

### 时间趋势（折线 / 柱状）

```http
GET /api/reports/trend?dateFrom=2026-06-01&dateTo=2026-06-14&interval=DAY
GET /api/reports/trend?dateFrom=2026-06-01&dateTo=2026-06-30&interval=WEEK
GET /api/reports/trend?dateFrom=2026-01-01&dateTo=2026-06-30&interval=MONTH
```

### 多维排名

```http
GET /api/reports/ranking?dateFrom=2026-06-01&dateTo=2026-06-14&groupBy=CHANNEL
GET /api/reports/ranking?dateFrom=2026-06-01&dateTo=2026-06-14&groupBy=SCENIC
GET /api/reports/ranking?dateFrom=2026-06-01&dateTo=2026-06-14&groupBy=TICKET
GET /api/reports/ranking?dateFrom=2026-06-01&dateTo=2026-06-14&groupBy=PAY_METHOD
GET /api/reports/ranking?dateFrom=2026-06-01&dateTo=2026-06-14&groupBy=WINDOW
```

### 检票转化漏斗

```http
GET /api/reports/visit-funnel?dateFrom=2026-06-01&dateTo=2026-06-14
```

返回：

```json
{
  "code": 200,
  "data": {
    "dateFrom": "2026-06-01",
    "dateTo": "2026-06-14",
    "steps": [
      { "step": 1, "name": "下单", "count": 1, "conversionRate": 100.00 },
      { "step": 2, "name": "出票", "count": 1, "conversionRate": 100.00 },
      { "step": 3, "name": "核销", "count": 0, "conversionRate": 0.00 }
    ],
    "orderToIssueRate": 100.00,
    "issueToUseRate": 0.00,
    "orderToUseRate": 0.00
  }
}
```

### 库存日报

```http
GET /api/reports/inventory?dateFrom=2026-06-14&dateTo=2026-06-21&scenicId=1
```

### 支付方式分布

```http
GET /api/reports/payment?dateFrom=2026-06-01&dateTo=2026-06-14
```

### 系统参数分页查询

```http
GET /api/settings?keyword=超时&groupName=订单&pageNum=1&pageSize=10
```

### 按 key 查参数

```http
GET /api/settings/by-key?key=ORDER_TIMEOUT_MIN
```

### 按分组批量取参数（用于前端"系统设置"页加载）

```http
GET /api/settings/by-group?group=订单
```

### 新增 / 修改参数

```http
POST /api/settings
Content-Type: application/json

{
  "settingKey": "ORDER_TIMEOUT_MIN",
  "settingValue": "45",
  "valueType": "NUMBER",
  "groupName": "订单",
  "description": "订单超时未支付自动取消（分钟）",
  "status": "启用"
}
```

返回：

```json
{ "code": 200, "data": 13 }
```

> 尝试修改 `SYS_VERSION`（只读）会返回 409 `SETTING_READONLY`。

### 切换参数状态

```http
PATCH /api/settings/13/status?status=停用
```

### 操作日志分页查询

```http
GET /api/op-logs?module=订单&action=退款&status=成功&opDateFrom=2026-06-01&opDateTo=2026-06-14&pageNum=1&pageSize=10
```

### 清理 N 天前的操作日志（运维）

```http
POST /api/op-logs/clean?retentionDays=180
```

返回：

```json
{ "code": 200, "data": 247 }
```

### 业务侧读取系统参数

```java
@Service
public class OrderServiceImpl {
    @Autowired private SettingService settingService;
    
    public void cancelExpiredPending() {
        int timeoutMin = settingService.getInt("ORDER_TIMEOUT_MIN", 30);
        // ... 用 timeoutMin 过滤超时订单
    }
}
```

### 业务侧记录操作日志

```java
@Service
public class OrderServiceImpl {
    @Autowired private OpLogService opLogService;
    
    @Transactional
    public void refund(Long id, OrderRefundDTO dto) {
        try {
            // ... 退款主流程
            opLogService.record(OpLogRecordDTO.builder()
                    .module("订单")
                    .action("退款")
                    .bizId(id)
                    .operatorName("财务小李")
                    .operatorRole("财务")
                    .status("成功")
                    .durationMs(System.currentTimeMillis() - start)
                    .build());
        } catch (Exception e) {
            opLogService.record(OpLogRecordDTO.builder()
                    .module("订单")
                    .action("退款")
                    .bizId(id)
                    .operatorName("财务小李")
                    .status("失败")
                    .errorMsg(e.getMessage())
                    .build());
            throw e;
        }
    }
}
```

### 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "青秀山风景区",
        "level": "国家5A级景区",
        "status": "运营中",
        ...
      }
    ],
    "total": 3,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "timestamp": 1749888000000
}
```

## 六、统一响应码

| 编码区间 | 含义             |
|---------|------------------|
| 200     | 成功             |
| 500     | 系统异常         |
| 1xxx    | 通用错误         |
| 20xx    | 园区业务错误     |
| 21xx    | 规则业务错误     |
| 22xx    | 票种业务错误     |
| 23xx    | 库存业务错误     |
| 24xx    | 窗口售票业务错误 |
| 25xx    | 检票业务错误     |
| 26xx    | 票据管理业务错误 |
| 27xx    | 订单管理业务错误 |
| 28xx    | 渠道管理业务错误 |
| 29xx    | 数据报表业务错误 |
| 30xx    | 系统设置业务错误 |
| 31xx    | 操作日志业务错误 |
| ...     | 后续按模块分配   |

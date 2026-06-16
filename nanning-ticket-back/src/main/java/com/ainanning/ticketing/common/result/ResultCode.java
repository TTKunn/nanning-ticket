package com.ainanning.ticketing.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务响应码枚举
 *
 * <p>编码规则：
 * <ul>
 *   <li>200：成功</li>
 *   <li>500：系统异常</li>
 *   <li>1xxx：通用错误（参数、找不到资源）</li>
 *   <li>20xx：园区业务错误</li>
 *   <li>21xx：项目规则业务错误</li>
 *   <li>22xx：票种业务错误</li>
 *   <li>...：后续按模块分配</li>
 * </ul>
 *
 * @author nanning-ticket
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    /* 通用错误 1xxx */
    PARAM_ERROR(1001, "参数校验失败"),
    NOT_FOUND(1004, "资源不存在"),

    /* 园区业务错误 20xx */
    SCENIC_NOT_FOUND(2001, "园区不存在"),
    SCENIC_NAME_DUPLICATE(2002, "园区名称已存在"),
    SCENIC_HAS_TICKETS(2003, "园区下存在有效票种，无法删除"),
    SCENIC_STATUS_INVALID(2004, "园区状态不合法"),

    /* 项目规则业务错误 21xx */
    RULE_NOT_FOUND(2101, "规则不存在"),
    RULE_CODE_DUPLICATE(2102, "同一园区下规则编码已存在"),
    RULE_TYPE_INVALID(2103, "规则类型不合法"),
    RULE_STATUS_INVALID(2104, "规则状态不合法"),
    RULE_EFFECTIVE_RANGE_INVALID(2105, "生效日期范围不合法"),
    RULE_SAVE_FAILED(2106, "保存规则失败"),
    RULE_DELETE_FAILED(2107, "删除规则失败"),

    /* 票种业务错误 22xx */
    TICKET_NOT_FOUND(2201, "票种不存在"),
    TICKET_CODE_DUPLICATE(2202, "同一园区下票种编码已存在"),
    TICKET_CATEGORY_INVALID(2203, "票种分类不合法"),
    TICKET_STATUS_INVALID(2204, "票种状态不合法"),
    TICKET_PRICE_INVALID(2205, "票面价必须大于 0"),
    TICKET_RULE_NOT_FOUND(2206, "关联的规则不存在或已删除"),
    TICKET_SAVE_FAILED(2207, "保存票种失败"),
    TICKET_DELETE_FAILED(2208, "删除票种失败"),
    TICKET_HAS_ORDERS(2209, "票种下存在有效订单，无法删除"),

    /* 库存业务错误 23xx */
    INVENTORY_NOT_FOUND(2301, "库存记录不存在"),
    INVENTORY_DATE_DUPLICATE(2302, "同票种同日期的库存已存在"),
    INVENTORY_DATE_INVALID(2303, "库存日期不合法"),
    INVENTORY_TOTAL_INVALID(2304, "总库存必须大于 0"),
    INVENTORY_STATUS_INVALID(2305, "库存状态不合法"),
    INVENTORY_HAS_SOLD(2306, "已存在销售记录，无法删除"),
    INVENTORY_DATE_RANGE_TOO_LARGE(2307, "日期范围不能超过 365 天"),
    INVENTORY_BATCH_FAILED(2308, "批量创建库存失败"),
    INVENTORY_SAVE_FAILED(2309, "保存库存失败"),
    INVENTORY_DELETE_FAILED(2310, "删除库存失败"),

    /* 窗口售票业务错误 24xx */
    SALE_NOT_FOUND(2401, "销售单不存在"),
    SALE_ITEM_NOT_FOUND(2402, "销售明细不存在"),
    SALE_TICKET_OFFLINE(2403, "票种未在售"),
    SALE_INVENTORY_NOT_FOUND(2404, "对应入场日期的库存不存在"),
    SALE_INVENTORY_CLOSED(2405, "库存已关闭或售罄"),
    SALE_STOCK_NOT_ENOUGH(2406, "可用库存不足"),
    SALE_QUANTITY_INVALID(2407, "购买数量不合法"),
    SALE_PAYMENT_INVALID(2408, "支付方式不合法"),
    SALE_AMOUNT_INVALID(2409, "销售金额不合法"),
    SALE_STATUS_INVALID(2410, "销售状态不合法"),
    SALE_ALREADY_REFUNDED(2411, "销售单已全单退票"),
    SALE_REFUND_EXCEED(2412, "退票数量超过原购买数量"),
    SALE_NO_ITEMS(2413, "销售明细不能为空"),
    SALE_TICKET_MISMATCH(2414, "销售明细中的票种与园区不匹配"),
    SALE_INVENTORY_DATE_INVALID(2415, "入场日期必须不早于今天"),
    SALE_SAVE_FAILED(2416, "保存销售单失败"),
    SALE_REFUND_FAILED(2417, "退票失败"),
    SALE_CANCEL_FAILED(2418, "取消销售单失败"),
    SALE_DUPLICATE_NO(2419, "销售流水号重复"),
    SALE_DELETE_FAILED(2420, "删除销售单失败"),

    /* 检票业务错误 25xx */
    VERIFY_RECORD_NOT_FOUND(2501, "检票记录不存在"),
    VERIFY_VOUCHER_NOT_FOUND(2502, "票据码无效"),
    VERIFY_VOUCHER_USED(2503, "票据已被使用"),
    VERIFY_VOUCHER_NOT_YET_VALID(2504, "票据入场日尚未生效"),
    VERIFY_VOUCHER_EXPIRED(2505, "票据已过入场有效期"),
    VERIFY_VOUCHER_SOLD_INVALID(2506, "销售单已退票或已取消"),
    VERIFY_RESULT_INVALID(2507, "检票结果不合法"),
    VERIFY_METHOD_INVALID(2508, "检票方式不合法"),
    VERIFY_SAVE_FAILED(2509, "保存检票记录失败"),

    /* 票据管理业务错误 26xx */
    VOUCHER_NOT_FOUND(2601, "票据不存在"),
    VOUCHER_CODE_DUPLICATE(2602, "票据码重复"),
    VOUCHER_STATUS_INVALID(2603, "票据状态不合法"),
    VOUCHER_ALREADY_USED(2604, "票据已使用，不能作废"),
    VOUCHER_ALREADY_REVOKED(2605, "票据已作废，请勿重复操作"),
    VOUCHER_EXPIRED(2606, "票据已过期，不能重发"),
    VOUCHER_SALE_NOT_FOUND(2607, "票据所属销售单不存在"),
    VOUCHER_SALE_NOT_REFUNDABLE(2608, "原销售单不可退"),
    VOUCHER_BATCH_EMPTY(2609, "批量操作 ID 列表不能为空"),
    VOUCHER_SAVE_FAILED(2610, "保存票据失败"),
    VOUCHER_REVOKE_FAILED(2611, "作废票据失败"),
    VOUCHER_REISSUE_FAILED(2612, "补发票据失败"),
    VOUCHER_GENERATE_FAILED(2613, "生成票据码失败"),

    /* 订单管理业务错误 27xx */
    ORDER_NOT_FOUND(2701, "订单不存在"),
    ORDER_NO_ITEMS(2702, "订单明细不能为空"),
    ORDER_ITEM_NOT_FOUND(2703, "订单明细不存在"),
    ORDER_STATUS_INVALID(2704, "订单状态不合法"),
    ORDER_ALREADY_PAID(2705, "订单已支付，请勿重复支付"),
    ORDER_NOT_PAID(2706, "订单未支付，不能退款"),
    ORDER_ALREADY_CANCELLED(2707, "订单已取消"),
    ORDER_ALREADY_REFUNDED(2708, "订单已全额退款"),
    ORDER_REFUND_EXCEED(2709, "退款数量超过原购买数量"),
    ORDER_QUANTITY_INVALID(2710, "订单数量不合法"),
    ORDER_AMOUNT_INVALID(2711, "订单金额不合法"),
    ORDER_PAY_METHOD_INVALID(2712, "支付方式不合法"),
    ORDER_CHANNEL_INVALID(2713, "渠道不合法"),
    ORDER_TICKET_OFFLINE(2714, "票种未在售"),
    ORDER_INVENTORY_NOT_FOUND(2715, "对应入场日期的库存不存在"),
    ORDER_INVENTORY_CLOSED(2716, "库存已关闭或售罄"),
    ORDER_STOCK_NOT_ENOUGH(2717, "可用库存不足"),
    ORDER_INVENTORY_DATE_INVALID(2718, "入场日期必须不早于今天"),
    ORDER_TICKET_MISMATCH(2719, "订单明细中的票种与园区不匹配"),
    ORDER_CONTACT_MISSING(2720, "联系人姓名与手机不能同时为空"),
    ORDER_PAY_FAILED(2721, "支付失败"),
    ORDER_CANCEL_FAILED(2722, "取消订单失败"),
    ORDER_REFUND_FAILED(2723, "退款失败"),
    ORDER_FULFILL_FAILED(2724, "出票失败"),
    ORDER_SAVE_FAILED(2725, "保存订单失败"),
    ORDER_DUPLICATE_NO(2726, "订单流水号重复"),
    ORDER_DATE_RANGE_INVALID(2727, "订单日期范围不合法"),
    ORDER_HAS_VOUCHER_USED(2728, "订单下有票据已被核销，不能退"),
    ORDER_PARTIAL_REFUND_NOT_SUPPORTED(2729, "暂不支持部分退款"),
    ORDER_DELETE_FAILED(2730, "删除订单失败"),

    /* 渠道管理业务错误 28xx */
    CHANNEL_NOT_FOUND(2801, "渠道不存在"),
    CHANNEL_CODE_DUPLICATE(2802, "渠道编码已存在"),
    CHANNEL_TYPE_INVALID(2803, "渠道类型不合法"),
    CHANNEL_STATUS_INVALID(2804, "渠道状态不合法"),
    CHANNEL_SAVE_FAILED(2805, "保存渠道失败"),
    CHANNEL_DELETE_FAILED(2806, "删除渠道失败"),
    CHANNEL_HAS_ORDERS(2807, "渠道下存在历史订单，无法删除"),
    CHANNEL_COMMISSION_INVALID(2808, "佣金比例不合法，必须在 0-100 之间"),
    CHANNEL_API_KEY_INVALID(2809, "API 密钥格式不合法"),
    CHANNEL_SETTLEMENT_NOT_FOUND(2810, "结算单不存在"),
    CHANNEL_SETTLEMENT_AMOUNT_INVALID(2811, "结算金额不合法"),
    CHANNEL_SETTLEMENT_STATUS_INVALID(2812, "结算单状态不合法"),
    CHANNEL_SETTLEMENT_CONFIRM_FAILED(2813, "确认结算单失败"),
    CHANNEL_SETTLEMENT_PAY_FAILED(2814, "打款结算单失败"),
    CHANNEL_SETTLEMENT_PERIOD_INVALID(2815, "结算周期不合法"),
    CHANNEL_ORDERS_EMPTY(2816, "结算周期内没有可结算的订单"),
    CHANNEL_STAT_PERIOD_INVALID(2817, "统计起止日期不合法"),

    /* 数据报表业务错误 29xx */
    REPORT_DATE_RANGE_INVALID(2901, "统计起止日期不合法"),
    REPORT_DATE_RANGE_TOO_LARGE(2902, "统计日期范围不能超过 366 天"),
    REPORT_DIMENSION_INVALID(2903, "统计维度不合法"),
    REPORT_GROUP_BY_INVALID(2904, "分组方式不合法"),
    REPORT_INTERVAL_INVALID(2905, "时间粒度不合法"),
    REPORT_METRIC_INVALID(2906, "指标类型不合法"),
    REPORT_QUERY_FAILED(2907, "报表查询失败"),
    REPORT_EMPTY_RESULT(2908, "统计结果为空"),

    /* 系统设置业务错误 30xx */
    SETTING_NOT_FOUND(3001, "系统参数不存在"),
    SETTING_KEY_DUPLICATE(3002, "参数键已存在"),
    SETTING_KEY_INVALID(3003, "参数键格式不合法（必须以字母开头，仅含字母数字下划线点）"),
    SETTING_VALUE_INVALID(3004, "参数值与声明类型不匹配"),
    SETTING_READONLY(3005, "只读参数不可修改"),
    SETTING_GROUP_INVALID(3006, "参数分组不合法"),
    SETTING_SAVE_FAILED(3007, "保存参数失败"),
    SETTING_DELETE_FAILED(3008, "删除参数失败"),
    SETTING_STATUS_INVALID(3009, "参数状态不合法"),

    /* 操作日志业务错误 31xx */
    OP_LOG_NOT_FOUND(3101, "操作日志不存在"),
    OP_LOG_MODULE_INVALID(3102, "操作模块不合法"),
    OP_LOG_ACTION_INVALID(3103, "操作动作不合法"),
    OP_LOG_STATUS_INVALID(3104, "操作日志状态不合法"),
    OP_LOG_PARAMS_TOO_LARGE(3105, "请求参数过大，不允许记录"),
    OP_LOG_RECORD_FAILED(3106, "记录操作日志失败"),
    OP_LOG_RETENTION_DAYS_INVALID(3107, "保留天数不合法（必须 1-3650）");

    private final Integer code;
    private final String message;
}

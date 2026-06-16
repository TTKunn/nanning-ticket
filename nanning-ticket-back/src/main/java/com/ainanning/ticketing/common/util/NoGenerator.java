package com.ainanning.ticketing.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * 业务编号生成工具
 *
 * <p>本项目所有"单号 / 流水号"的统一入口，针对原型阶段已识别的并发问题做了如下处理：
 * <ul>
 *   <li>格式：{@code 前缀 + yyyyMMdd[HHmmss] + N 位随机}</li>
 *   <li>DB 唯一键冲突时由调用方传入 {@link Function} 检查并重试（最多 3 次），
 *       避免直接抛出 DuplicateKeyException 导致整事务回滚</li>
 *   <li>提供 {@link #uuidShort()} 作为兜底（高并发场景下用 UUID 取前 N 位）</li>
 * </ul>
 *
 * <p>典型用法：
 * <pre>{@code
 * String orderNo = NoGenerator.generateWithRetry(
 *     "O", 3, NoGenerator::todayCompact, code -> orderMapper.existsByOrderNo(code));
 * }</pre>
 *
 * @author nanning-ticket
 */
public final class NoGenerator {

    private NoGenerator() {}

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YMDHMS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 当天紧凑日期 e.g. {@code 20260614} */
    public static String todayCompact() {
        return LocalDate.now().format(YMD);
    }

    /** 当前时间戳 e.g. {@code 20260614091234} */
    public static String nowCompact() {
        return LocalDateTime.now().format(YMDHMS);
    }

    /** UUID 取前 12 位（大写） */
    public static String uuidShort() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    /**
     * 生成带 N 位随机数的流水号
     *
     * @param prefix   前缀，如 {@code "O"} / {@code "S"} / {@code "V"}
     * @param middle   中间日期/时间戳字符串
     * @param randSize 随机数位数（建议 ≥3）
     * @return 形如 {@code O20260614091234512}（前缀+中段+3 位随机）
     */
    public static String build(String prefix, String middle, int randSize) {
        int max = (int) Math.pow(10, randSize);
        int rand = ThreadLocalRandom.current().nextInt(max);
        return prefix + middle + String.format("%0" + randSize + "d", rand);
    }

    /**
     * 生成流水号并自动重试（DB 唯一键冲突兜底）
     *
     * @param prefix    前缀
     * @param middle    中段字符串
     * @param randSize  随机数位数
     * @param existsFn  检测单号是否已存在；返回 true 表示冲突
     * @return 不冲突的流水号；若重试 N 次仍冲突则降级使用 UUID 短串
     */
    public static String generateWithRetry(String prefix, String middle, int randSize,
                                           Function<String, Boolean> existsFn) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            String no = build(prefix, middle, randSize);
            if (!existsFn.apply(no)) {
                return no;
            }
        }
        // 兜底：使用 UUID 短串，确保不冲突
        return prefix + middle + uuidShort().substring(0, Math.min(randSize, 12));
    }

    /**
     * 按当天日期生成的简易版本（适合 "S + yyyyMMdd + 4 位随机" 这类格式）
     */
    public static String generateDaily(String prefix, int randSize,
                                       Function<String, Boolean> existsFn) {
        return generateWithRetry(prefix, todayCompact(), randSize, existsFn);
    }
}

package com.ainanning.ticketing.service;

import com.ainanning.ticketing.dto.ReportQueryDTO;
import com.ainanning.ticketing.vo.ReportInventoryVO;
import com.ainanning.ticketing.vo.ReportOverviewVO;
import com.ainanning.ticketing.vo.ReportPaymentVO;
import com.ainanning.ticketing.vo.ReportRankingVO;
import com.ainanning.ticketing.vo.ReportTrendVO;
import com.ainanning.ticketing.vo.ReportVisitFunnelVO;

/**
 * 数据报表业务接口
 *
 * <p>本模块 = 纯读聚合层，不写新表，所有指标从已有业务表（scenic / ticket / inventory /
 * sale / order / voucher / verify_record / channel / channel_settlement）聚合而来。
 * <br>5 个核心接口：
 * <ul>
 *   <li>{@link #overview} - 顶部数字卡（核心指标）</li>
 *   <li>{@link #trend} - 时间序列（折线图 / 柱状图）</li>
 *   <li>{@link #ranking} - 多维排名（渠道 / 园区 / 票种 / 支付 / 窗口）</li>
 *   <li>{@link #visitFunnel} - 检票转化漏斗</li>
 *   <li>{@link #inventory} - 库存日报</li>
 *   <li>{@link #payment} - 支付方式分布</li>
 * </ul>
 * </p>
 *
 * @author nanning-ticket
 */
public interface ReportService {

    /** 顶部核心指标 */
    ReportOverviewVO overview(ReportQueryDTO query);

    /** 时间序列趋势 */
    ReportTrendVO trend(ReportQueryDTO query);

    /** 多维排名 */
    ReportRankingVO ranking(ReportQueryDTO query);

    /** 检票转化漏斗 */
    ReportVisitFunnelVO visitFunnel(ReportQueryDTO query);

    /** 库存日报 */
    ReportInventoryVO inventory(ReportQueryDTO query);

    /** 支付方式分布 */
    ReportPaymentVO payment(ReportQueryDTO query);
}

package com.ainanning.ticketing.common.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页响应
 *
 * @author nanning-ticket
 */
@Data
@Builder
@Schema(description = "分页响应")
public class PageVO<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Long pageNum;

    @Schema(description = "每页大小")
    private Long pageSize;

    @Schema(description = "总页数")
    private Long pages;

    /** 将 MyBatis-Plus 的 IPage 转换为 VO（同类型 records） */
    public static <T> PageVO<T> of(IPage<T> page) {
        if (page == null) {
            return PageVO.<T>builder()
                    .records(Collections.emptyList())
                    .total(0L).pageNum(1L).pageSize(10L).pages(0L)
                    .build();
        }
        return PageVO.<T>builder()
                .records(page.getRecords())
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .pages(page.getPages())
                .build();
    }

    /**
     * 将 MyBatis-Plus 的 IPage 转换为 VO（records 已在外层转换为目标类型）
     *
     * <p>典型用法：分页查出的 Entity 列表需要在 Service 层做字段映射或批量注入关联名，
     * 此时 Entity 类型与最终 VO 类型不一致（{@code IPage<Entity> → PageVO<VO>}），
     * 不能直接调用 {@link #of(IPage)}，应使用本方法并传入已转换的 records。</p>
     *
     * @param page    MyBatis-Plus 分页对象（仅取元数据）
     * @param records 已转换为目标类型的记录列表
     */
    public static <T> PageVO<T> of(IPage<?> page, List<T> records) {
        if (page == null) {
            return PageVO.<T>builder()
                    .records(records == null ? Collections.emptyList() : records)
                    .total(0L).pageNum(1L).pageSize(10L).pages(0L)
                    .build();
        }
        return PageVO.<T>builder()
                .records(records)
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .pages(page.getPages())
                .build();
    }
}

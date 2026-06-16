package com.ainanning.ticketing.common.exception;

import com.ainanning.ticketing.common.result.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常
 *
 * <p>用于受检业务逻辑失败（如：参数错误、资源不存在、状态非法等），
 * 由 {@code GlobalExceptionHandler} 统一捕获并转换为 API 响应。</p>
 *
 * @author nanning-ticket
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}

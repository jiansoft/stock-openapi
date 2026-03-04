package com.jiansoft.stock_api.support;

import com.jiansoft.stock_api.dto.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全域 API 例外處理器。
 *
 * <p>統一將系統例外轉為固定格式的 JSON 錯誤回應。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 處理可直接回傳訊息的自訂例外。
     *
     * @param ex 自訂例外
     * @return 統一格式的錯誤回應
     */
    @ExceptionHandler(MessageException.class)
    public ResponseEntity<ErrorResponse> handleMessageException(MessageException ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }

    /**
     * 處理常見的請求參數錯誤。
     *
     * @param ex 參數轉換或驗證相關例外
     * @return 統一格式的錯誤回應
     */
    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentNotValidException.class,
        ConversionFailedException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request parameter."));
    }

    /**
     * 處理未預期的例外。
     *
     * @param ex 未預期的例外
     * @param request 當前 HTTP 請求
     * @return 統一格式的錯誤回應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(
        Exception ex,
        HttpServletRequest request
    ) {
        if (request.getRequestURI().startsWith("/api")) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error. Please try again later."
                ));
        }

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error. Please try again later."
            ));
    }
}

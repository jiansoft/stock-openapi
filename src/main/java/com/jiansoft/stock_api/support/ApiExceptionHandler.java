package com.jiansoft.stock_api.support;

import com.jiansoft.stock_api.dto.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全域 API 例外處理器。
 *
 * <p>統一將系統例外轉為固定格式的 JSON 錯誤回應。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

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
        ConversionFailedException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        String message = switch (ex) {
            case MethodArgumentTypeMismatchException mismatchException ->
                "Invalid request parameter '%s'.".formatted(mismatchException.getName());
            case MissingServletRequestParameterException missingParameterException ->
                "Missing required request parameter '%s'.".formatted(missingParameterException.getParameterName());
            case MethodArgumentNotValidException ignored -> "Invalid request parameter.";
            case ConversionFailedException ignored -> "Invalid request parameter.";
            case IllegalArgumentException illegalArgumentException ->
                StringUtils.hasText(illegalArgumentException.getMessage())
                    ? illegalArgumentException.getMessage()
                    : "Invalid request parameter.";
            default -> "Invalid request parameter.";
        };

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
    }

    /**
     * 處理找不到資源的請求。
     *
     * @param ex 找不到資源的例外
     * @param request 原始 HTTP 請求
     * @return 統一格式的 404 錯誤回應
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        if (!shouldRenderJsonNotFoundResponse(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found."));
    }

    /**
     * 處理未預期的例外。
     *
     * @param ex 未預期的例外
     * @param request 原始 HTTP 請求
     * @return 統一格式的錯誤回應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception ex, HttpServletRequest request) {
        logger.error(
            "Unhandled exception while processing {} {}",
            request.getMethod(),
            buildRequestTarget(request),
            ex
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error. Please try again later."
            ));
    }

    private boolean shouldRenderJsonNotFoundResponse(HttpServletRequest request) {
        return isApiRequest(request) && acceptsJson(request);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String apiPrefix = request.getContextPath() + "/api/";
        return StringUtils.hasText(requestUri) && requestUri.startsWith(apiPrefix);
    }

    private boolean acceptsJson(HttpServletRequest request) {
        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        if (!StringUtils.hasText(acceptHeader)) {
            return true;
        }

        try {
            return MediaType.parseMediaTypes(acceptHeader).stream()
                .anyMatch(mediaType -> mediaType.isCompatibleWith(MediaType.APPLICATION_JSON));
        } catch (InvalidMediaTypeException ignored) {
            return false;
        }
    }

    private String buildRequestTarget(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        return StringUtils.hasText(queryString) ? requestUri + "?" + queryString : requestUri;
    }
}

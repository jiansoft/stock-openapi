package com.jiansoft.stock_api.support;

/**
 * 用於攜帶可直接回傳給 API 呼叫端之訊息的執行期例外。
 */
public class MessageException extends RuntimeException {

    /**
     * 建立僅含訊息的例外。
     *
     * @param message 例外訊息
     */
    public MessageException(String message) {
        super(message);
    }

    /**
     * 建立含有訊息與原始例外的例外。
     *
     * @param message 例外訊息
     * @param cause 原始例外
     */
    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }
}

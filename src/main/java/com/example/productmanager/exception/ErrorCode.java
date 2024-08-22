package com.example.productmanager.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "error.uncategorized", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "error.invalidInput", HttpStatus.BAD_REQUEST),
    INVALID_NAME(1002, "error.invalid", HttpStatus.NOT_FOUND),
    CATEGORY_LIST_NOT_FOUND(1003,"error.categoryList.notFound", HttpStatus.NOT_FOUND),
    ERROR_ADD_CATEGORY(1004, "error.add.category", HttpStatus.BAD_REQUEST),



    ;


    private final int code;
    private final String messageKey;
    private final HttpStatus statusCode;

    ErrorCode(int code, String messageKey, HttpStatus statusCode) {
        this.code = code;
        this.messageKey = messageKey;
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }
}

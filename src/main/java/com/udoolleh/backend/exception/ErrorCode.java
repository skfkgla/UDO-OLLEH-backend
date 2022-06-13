package com.udoolleh.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    NOT_FOUND_PATH(HttpStatus.NOT_FOUND, "PATH_001", "NOT FOUND PATH"), // 없는 경로로 요청보낸 경우
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,"PATH_002","METHOD NOT ALLOWED"), // POST GET방식 잘못 보낸경우
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "PATH_003", "UNSUPPORTED MEDIA TYPE"),
    REQUEST_PARAMETER_BIND_FAILED(HttpStatus.BAD_REQUEST, "REQ_001", "PARAMETER_BIND_FAILED"),
    WHARF_NAME_DUPLICATED(HttpStatus.FORBIDDEN, "WHARF_001", "선착장 이름이 중복됨");

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(final HttpStatus status, final String code, final String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
package com.skhu.skhucapstone.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.skhu.skhucapstone.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("INVALID_REQUEST", message));
    }

    // 요청 본문을 객체로 바꾸지 못한 경우. 값의 형식이 맞지 않거나 JSON이 깨졌을 때다.
    // Spring 기본 처리는 로그를 남기지 않아 원인을 추적할 수 없으므로 직접 처리한다.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        String message = describeInvalidFormat(e);

        log.warn("요청 본문을 읽지 못했다: {}", message, e);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("INVALID_REQUEST_BODY", message));
    }

    // 경로 변수나 쿼리 파라미터의 타입이 맞지 않는 경우.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = e.getName() + " 값이 올바르지 않습니다.";

        log.warn("요청 파라미터 형식이 올바르지 않다: {}={}", e.getName(), e.getValue());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("INVALID_REQUEST_PARAM", message));
    }

    // 필수 값 누락이나 중복처럼 DB 제약에 걸린 경우.
    // 그대로 두면 본문 없는 500으로 나가 프론트가 원인을 알 수 없다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("데이터 제약 조건에 걸렸다", e);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        "DATA_INTEGRITY_VIOLATION",
                        "필수 값이 비어 있거나 이미 등록된 값입니다. 입력 내용을 확인해주세요."));
    }

    // 어떤 값이 왜 틀렸는지까지 알려준다. 특히 정해진 값만 받는 항목은
    // 허용 목록을 함께 내려줘야 프론트가 바로 고칠 수 있다.
    private String describeInvalidFormat(HttpMessageNotReadableException e) {
        if (!(e.getCause() instanceof InvalidFormatException cause)) {
            return "요청 형식이 올바르지 않습니다.";
        }

        String fieldName = cause.getPath().isEmpty()
                ? "요청 값"
                : cause.getPath().get(cause.getPath().size() - 1).getFieldName();

        if (cause.getTargetType() != null && cause.getTargetType().isEnum()) {
            String allowed = Arrays.stream(cause.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            return fieldName + " 값이 올바르지 않습니다. "
                    + "입력값: '" + cause.getValue() + "', 허용값: " + allowed;
        }

        return fieldName + " 값의 형식이 올바르지 않습니다. 입력값: '" + cause.getValue() + "'";
    }
}

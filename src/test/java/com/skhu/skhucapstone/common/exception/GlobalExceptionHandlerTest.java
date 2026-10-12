package com.skhu.skhucapstone.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.skhu.skhucapstone.coffeechat.entity.MeetingType;
import com.skhu.skhucapstone.coffeechat.dto.req.CoffeeChatProfileSaveReq;
import com.skhu.skhucapstone.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.assertj.core.api.Assertions.assertThat;

// 요청 값이 잘못됐을 때 프론트가 원인을 알 수 있는 응답이 나가는지 검증한다.
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpMessageNotReadableException enumParseFailure(String wrongValue) {
        InvalidFormatException cause = InvalidFormatException.from(
                null, "enum 변환 실패", wrongValue, MeetingType.class);

        cause.prependPath(new JsonMappingException.Reference(
                CoffeeChatProfileSaveReq.class, "meetingType"));

        return new HttpMessageNotReadableException("읽기 실패", cause, null);
    }

    @Test
    @DisplayName("정해진 값이 아닌 값을 보내면 어떤 항목이 틀렸고 무엇을 보내야 하는지 알려준다")
    void tellsFieldNameAndAllowedValues() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleNotReadable(enumParseFailure("대면"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_REQUEST_BODY");
        assertThat(response.getBody().getMessage())
                .contains("meetingType")
                .contains("대면")
                .contains("ONLINE")
                .contains("OFFLINE")
                .contains("ONLINE_OFFLINE");
    }

    @Test
    @DisplayName("원인을 알 수 없는 형식 오류도 본문 있는 400으로 내려간다")
    void fallsBackToGenericMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleNotReadable(
                new HttpMessageNotReadableException("깨진 JSON", (Throwable) null, null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("요청 형식이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("DB 제약 위반은 본문 없는 500이 아니라 안내 가능한 400으로 내려간다")
    void dataIntegrityBecomesReadableError() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("Column 'student_id' cannot be null"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("DATA_INTEGRITY_VIOLATION");
        assertThat(response.getBody().isSuccess()).isFalse();
        // DB 메시지를 그대로 노출하지 않는다.
        assertThat(response.getBody().getMessage()).doesNotContain("student_id");
    }
}

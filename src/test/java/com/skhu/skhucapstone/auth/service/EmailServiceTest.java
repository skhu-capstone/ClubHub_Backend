package com.skhu.skhucapstone.auth.service;

import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 인증 메일이 HTML과 텍스트 본문을 함께 담아 나가는지 검증한다.
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String SENDER = "clubhubb.noreply@gmail.com";
    private static final String RECEIVER = "test@office.skhu.ac.kr";

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", SENDER);
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
    }

    @Test
    @DisplayName("인증번호와 유효시간이 HTML과 텍스트 본문에 모두 담긴다")
    void containsCodeInBothBodies() throws Exception {
        emailService.sendVerificationCode(RECEIVER, "48213", 10);

        MimeMessage sent = captureSent();
        List<String> bodies = collectBodies(sent);

        // HTML을 못 여는 메일 앱을 위해 본문이 두 벌이어야 한다.
        assertThat(bodies).hasSize(2);
        assertThat(bodies).allSatisfy(body -> assertThat(body)
                .contains("48213")
                .contains("10분"));
        assertThat(bodies).anySatisfy(body -> assertThat(body).contains("<html"));
    }

    @Test
    @DisplayName("템플릿의 치환자가 메일에 그대로 남지 않는다")
    void leavesNoPlaceholder() throws Exception {
        emailService.sendVerificationCode(RECEIVER, "48213", 10);

        assertThat(collectBodies(captureSent()))
                .allSatisfy(body -> assertThat(body).doesNotContain("{{"));
    }

    @Test
    @DisplayName("받은편지함에 발신자가 ClubHub로 보인다")
    void showsServiceNameAsSender() throws Exception {
        emailService.sendVerificationCode(RECEIVER, "48213", 10);

        InternetAddress from = (InternetAddress) captureSent().getFrom()[0];
        assertThat(from.getPersonal()).isEqualTo("ClubHub");
        assertThat(from.getAddress()).isEqualTo(SENDER);
    }

    @Test
    @DisplayName("발송에 실패하면 원인을 알 수 있는 에러로 바꿔 던진다")
    void wrapsSendFailure() {
        doThrow(new MailSendException("SMTP 연결 실패"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> emailService.sendVerificationCode(RECEIVER, "48213", 10))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_SEND_FAILED);
    }

    private MimeMessage captureSent() {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    // multipart 안쪽까지 내려가 문자열 본문만 모은다.
    private List<String> collectBodies(Part part) throws Exception {
        List<String> bodies = new ArrayList<>();
        Object content = part.getContent();

        if (content instanceof String text) {
            bodies.add(text);
        } else if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                bodies.addAll(collectBodies(multipart.getBodyPart(i)));
            }
        }
        return bodies;
    }
}

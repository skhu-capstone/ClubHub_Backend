package com.skhu.skhucapstone.auth.service;

import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String SENDER_NAME = "ClubHub";
    private static final String SUBJECT = "[ClubHub] 학교 이메일 인증번호 안내";

    // 받는 사람은 한국에 있으므로 서버 시간대와 상관없이 한국 시각으로 보여준다.
    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter EXPIRES_AT_FORMAT = DateTimeFormatter.ofPattern("M월 d일 HH:mm");

    // 템플릿은 서버가 뜰 때 한 번만 읽는다. 파일이 없으면 기동 단계에서 바로 실패하므로
    // 운영 중에 메일을 보내다가 뒤늦게 알게 되는 일이 없다.
    private static final String VERIFICATION_TEMPLATE = loadTemplate("mail/verification-code.html");

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code, int expiryMinutes) {
        String expiresAt = LocalDateTime.now(KOREA).plusMinutes(expiryMinutes).format(EXPIRES_AT_FORMAT);

        // 메일이 늦게 온다는 제보가 있었는데 발송에 걸린 시간을 남기지 않아 확인할 수 없었다.
        // 우리 쪽이 느린 것인지, 받는 메일 서버가 늦게 받아주는 것인지 구분하려면 이 값이 필요하다.
        long startedAt = System.currentTimeMillis();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            // multipart로 보내야 HTML과 텍스트 본문을 함께 담을 수 있다.
            // HTML을 못 여는 메일 앱이나 텍스트만 보는 설정에서는 텍스트 본문이 보인다.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail, SENDER_NAME);
            helper.setTo(toEmail);
            helper.setSubject(SUBJECT);
            helper.setText(
                    buildPlainText(code, expiryMinutes, expiresAt),
                    buildHtml(code, expiryMinutes, expiresAt));

            mailSender.send(message);
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.warn("인증번호 메일 발송에 실패했다. 받는사람={}, 걸린시간={}ms",
                    toEmail, System.currentTimeMillis() - startedAt, e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

        log.info("인증번호 메일을 보냈다. 받는사람={}, 걸린시간={}ms",
                toEmail, System.currentTimeMillis() - startedAt);
    }

    // 인증번호는 숫자만, 나머지는 서버가 만든 값이라 HTML 이스케이프 없이 그대로 넣는다.
    // 사용자가 입력한 문자열을 넣게 되면 반드시 이스케이프해야 한다.
    static String buildHtml(String code, int expiryMinutes, String expiresAt) {
        return VERIFICATION_TEMPLATE
                .replace("{{code}}", code)
                .replace("{{expiryMinutes}}", String.valueOf(expiryMinutes))
                .replace("{{expiresAt}}", expiresAt);
    }

    static String buildPlainText(String code, int expiryMinutes, String expiresAt) {
        return """
                [ClubHub] 학교 이메일 인증번호

                아래 인증번호를 ClubHub 화면에 입력해주세요.

                인증번호: %s

                인증번호는 %d분 동안 유효합니다. (%s까지)

                본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.
                인증번호는 누구에게도 알려주지 마세요.

                이 메일은 발신 전용이라 회신할 수 없습니다.
                https://www.clubhubb.co.kr
                """.formatted(code, expiryMinutes, expiresAt);
    }

    private static String loadTemplate(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("메일 템플릿을 읽지 못했다: " + path, e);
        }
    }
}

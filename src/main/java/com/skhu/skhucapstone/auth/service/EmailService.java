package com.skhu.skhucapstone.auth.service;

import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code, int expiryMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[클럽허브] 학교 이메일 인증번호");
        message.setText("인증번호: " + code
                + "\n\n인증번호는 " + expiryMinutes + "분 후 만료됩니다.");

        // 메일이 늦게 온다는 제보가 있었는데 발송에 걸린 시간을 남기지 않아 확인할 수 없었다.
        // 우리 쪽이 느린 것인지, 받는 메일 서버가 늦게 받아주는 것인지 구분하려면 이 값이 필요하다.
        long startedAt = System.currentTimeMillis();

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("인증번호 메일 발송에 실패했다. 받는사람={}, 걸린시간={}ms",
                    toEmail, System.currentTimeMillis() - startedAt, e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

        log.info("인증번호 메일을 보냈다. 받는사람={}, 걸린시간={}ms",
                toEmail, System.currentTimeMillis() - startedAt);
    }
}

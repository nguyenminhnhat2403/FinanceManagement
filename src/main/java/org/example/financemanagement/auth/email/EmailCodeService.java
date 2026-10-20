package org.example.financemanagement.auth.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeService {

    public static final String REGISTER = "REGISTER";
    public static final String RESET_PASSWORD = "RESET_PASSWORD";

    private final EmailCodeRepository repository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.mail.from:${spring.mail.username}}")
    private String senderEmail;

    public String normalizeEmail(String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);

        if (!normalized.endsWith("@gmail.com")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Hãy nhập địa chỉ @gmail.com"
            );
        }

        return normalized;
    }

    @Transactional
    public UUID sendCode(String inputEmail, String purpose) {
        String email = normalizeEmail(inputEmail);
        Instant now = Instant.now();

        // Giới hạn cơ bản cho bản local: 3 yêu cầu/email/mục đích/15 phút.
        long recent = repository.countByEmailAndPurposeAndCreatedAtAfter(
                email, purpose, now.minus(15, ChronoUnit.MINUTES)
        );

        if (recent >= 3) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Bạn yêu cầu mã quá nhiều lần. Vui lòng thử lại sau."
            );
        }

        String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));

        EmailCode challenge = new EmailCode();

        challenge.setId(UUID.randomUUID());
        challenge.setEmail(email);
        challenge.setPurpose(purpose);
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setCreatedAt(now);
        challenge.setExpiresAt(now.plus(10, ChronoUnit.MINUTES));
        challenge.setAttempts(0);
        challenge.setConsumed(false);

        repository.saveAndFlush(challenge);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(email);
        message.setSubject(REGISTER.equals(purpose)
                ? "Mã xác nhận đăng ký FinanceManagement"
                : "Mã đặt lại mật khẩu FinanceManagement");
        message.setText(
                "Mã xác nhận của bạn: " + code +
                        "\nMã có hiệu lực trong 10 phút và chỉ dùng một lần." +
                        "\nKhông chia sẻ mã này với người khác." +
                        "\nNếu bạn không yêu cầu, hãy bỏ qua email."
        );

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Gửi email xác nhận thất bại", ex);

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa gửi được email. Vui lòng thử lại sau."
            );
        }

        // Chỉ trả ID phiên xác minh, tuyệt đối không trả mã về frontend.
        return challenge.getId();
    }

    // Phải được gọi bên trong transaction tạo user hoặc đổi mật khẩu.
    @Transactional(propagation = Propagation.MANDATORY,
            noRollbackFor = InvalidEmailCodeException.class)
    public void consume(UUID challengeId, String inputEmail, String code, String purpose) {
        String email = normalizeEmail(inputEmail);

        EmailCode challenge = repository.findForUpdate(challengeId)
                .orElseThrow(InvalidEmailCodeException::new);

        if (!challenge.getEmail().equals(email)
                || !challenge.getPurpose().equals(purpose)
                || challenge.isConsumed()
                || !Instant.now().isBefore(challenge.getExpiresAt())
                || challenge.getAttempts() >= 5) {
            throw new InvalidEmailCodeException();
        }

        if (!passwordEncoder.matches(code, challenge.getCodeHash())) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            throw new InvalidEmailCodeException();
        }

        challenge.setConsumed(true);
    }
}


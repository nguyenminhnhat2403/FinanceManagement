package org.example.financemanagement.auth.email;

import lombok.RequiredArgsConstructor;
import org.example.financemanagement.user.User;
import org.example.financemanagement.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final EmailCodeService emailCodeService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(noRollbackFor = InvalidEmailCodeException.class)
    public void reset(ResetPasswordRequest request) {
        if (request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mật khẩu vượt quá giới hạn 72 byte."
            );
        }

        String email = emailCodeService.normalizeEmail(request.email());

        emailCodeService.consume(
                request.challengeId(), email,
                request.code(), EmailCodeService.RESET_PASSWORD
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidEmailCodeException::new);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}


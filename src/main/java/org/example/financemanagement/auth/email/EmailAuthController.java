package org.example.financemanagement.auth.email;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailAuthController {

    private final EmailCodeService emailCodeService;
    private final PasswordResetService passwordResetService;

    public record CodeResponse(UUID challengeId, String message) {}

    @PostMapping("/register-code")
    public CodeResponse registerCode(@Valid @RequestBody EmailRequest request) {
        UUID id = emailCodeService.sendCode(
                request.email(), EmailCodeService.REGISTER
        );
        return new CodeResponse(id, "Đã gửi mã xác nhận. Hãy kiểm tra Gmail.");
    }

    @PostMapping("/forgot-password")
    public CodeResponse forgotPassword(@Valid @RequestBody EmailRequest request) {
        // Gửi cùng loại phản hồi/email dù tài khoản đã tồn tại hay chưa.
        // Chỉ mã hợp lệ + tài khoản đã tồn tại mới đổi được mật khẩu.
        UUID id = emailCodeService.sendCode(
                request.email(), EmailCodeService.RESET_PASSWORD
        );
        return new CodeResponse(id,
                "Đã gửi mã. Nếu email có tài khoản, bạn có thể dùng mã để đặt lại mật khẩu.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.reset(request);
        return Map.of("message", "Đã đổi mật khẩu. Hãy đăng nhập lại.");
    }
}


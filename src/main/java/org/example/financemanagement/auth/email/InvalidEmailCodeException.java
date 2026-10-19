package org.example.financemanagement.auth.email;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidEmailCodeException extends ResponseStatusException {
    public InvalidEmailCodeException() {
        super(HttpStatus.BAD_REQUEST,
                "Mã không hợp lệ, đã hết hạn hoặc vượt số lần thử. Hãy yêu cầu mã mới.");
    }
}


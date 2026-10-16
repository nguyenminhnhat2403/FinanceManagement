package org.example.financemanagement.security;

import org.example.financemanagement.user.User;
import org.example.financemanagement.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
public class UserAccessService {

    private final UserRepository userRepository;

    public UserAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User checkOwner(Long requestedUserId) {

        // Lấy tài khoản mà JWT filter đã xác thực
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Bạn chưa đăng nhập"
            );
        }

        // Trong project hiện tại, username là email
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Tài khoản không còn tồn tại"
                ));

        // So sánh ID tài khoản đang đăng nhập với ID trên URL
        if (!Objects.equals(
                Long.valueOf(currentUser.getId()),
                requestedUserId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền truy cập dữ liệu của người khác"
            );
        }

        return currentUser;
    }
}
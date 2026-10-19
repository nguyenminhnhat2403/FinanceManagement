package org.example.financemanagement.auth;

import org.example.financemanagement.auth.email.EmailCodeService;
import org.example.financemanagement.security.JwtService;
import org.example.financemanagement.user.User;
import org.example.financemanagement.user.UserRepository;
import org.example.financemanagement.user.UserService;
import org.example.financemanagement.user.dto.CreateUserRequest;
import org.example.financemanagement.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.example.financemanagement.auth.email.EmailCodeService;
import org.example.financemanagement.auth.email.InvalidEmailCodeException;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService ;
    private final AuthenticationManager authenticationManager;
    private final EmailCodeService emailCodeService;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            EmailCodeService emailCodeService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailCodeService = emailCodeService;
    }

    @Transactional(noRollbackFor = InvalidEmailCodeException.class)
    public UserResponse register(RegisterRequest request) {
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mật khẩu vượt quá giới hạn 72 byte."
            );
        }

        String email = emailCodeService.normalizeEmail(request.getEmail());

        emailCodeService.consume(
                request.getChallengeId(),
                email,
                request.getCode(),
                EmailCodeService.REGISTER
        );

        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName(request.getName().trim());
        createRequest.setEmail(email);
        createRequest.setPassword(request.getPassword());

        return userService.createUser(createRequest);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                token
        );
    }
}

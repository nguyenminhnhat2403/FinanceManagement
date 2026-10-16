package org.example.financemanagement.auth;

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

@Service
public class AuthService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService ;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public UserResponse register(RegisterRequest request) {
        CreateUserRequest createRequest = new CreateUserRequest();

        createRequest.setName(request.getName());
        createRequest.setEmail(request.getEmail());
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

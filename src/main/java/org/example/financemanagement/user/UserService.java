package org.example.financemanagement.user;

import org.example.financemanagement.common.exception.UserNotFoundException;
import org.example.financemanagement.security.UserAccessService;
import org.example.financemanagement.user.dto.CreateUserRequest;
import org.example.financemanagement.user.dto.UpdateUserRequest;
import org.example.financemanagement.user.dto.UserDTOMapper;
import org.example.financemanagement.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserAccessService userAccessService;

    public UserService(UserRepository userRepository,
                       UserDTOMapper userDTOMapper,
                       PasswordEncoder passwordEncoder,
                       UserAccessService userAccessService) {
        this.userRepository = userRepository;
        this.userDTOMapper = userDTOMapper;
        this.passwordEncoder = passwordEncoder;
        this.userAccessService = userAccessService;
    }

    public UserResponse createUser(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email đã được sử dụng"
            );
        }
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        User savedUser =userRepository.save(user);
        return userDTOMapper.apply(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();
        for(User user : users) {
            responses.add(userDTOMapper.apply(user));
        }
        return responses;
    }

    public UserResponse getUserById(Long id) {
        User user = userAccessService.checkOwner(id);

        return userDTOMapper.apply(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {

        User user = userAccessService.checkOwner(id);

        user.setName(request.getName());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);

        return userDTOMapper.apply(savedUser);
    }

}

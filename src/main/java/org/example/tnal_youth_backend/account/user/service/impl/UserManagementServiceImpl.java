package org.example.tnal_youth_backend.account.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.user.dto.request.CreateUserRequest;
import org.example.tnal_youth_backend.account.user.dto.request.UpdateUserRequest;
import org.example.tnal_youth_backend.account.user.dto.response.UserResponse;
import org.example.tnal_youth_backend.account.user.mapper.UserMapper;
import org.example.tnal_youth_backend.account.user.service.UserManagementService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementServiceImpl
        implements UserManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /*
     * ==========================================================
     * GET ALL USERS
     * ==========================================================
     */

    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository
                .findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /*
     * ==========================================================
     * GET USER BY ID
     * ==========================================================
     */

    @Override
    public UserResponse getUserById(Long id) {

        User user = findUserById(id);

        return userMapper.toResponse(user);
    }

    /*
     * ==========================================================
     * CREATE USER
     * ==========================================================
     */

    @Override
    @Transactional
    public UserResponse createUser(
            CreateUserRequest request
    ) {
        String phone = normalize(request.phone());
        String email = normalizeEmail(request.email());

        validatePhoneOrEmail(phone, email);
        validatePhoneForCreate(phone);
        validateEmailForCreate(email);

        User user = User.builder()
                .phone(phone)
                .email(email)
                .passwordHash(
                        passwordEncoder.encode(
                                request.password()
                        )
                )
                .role(request.role())
                .status(request.status())
                .fullNameKm(
                        requireText(
                                request.fullNameKm(),
                                "Khmer full name is required"
                        )
                )
                .fullNameEn(
                        normalize(request.fullNameEn())
                )
                .profileImage(
                        normalize(request.profileImage())
                )
                .failedLoginCount(0)
                .build();

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    /*
     * ==========================================================
     * UPDATE USER
     * ==========================================================
     */

    @Override
    @Transactional
    public UserResponse updateUser(
            Long id,
            UpdateUserRequest request
    ) {
        User user = findUserById(id);

        String phone = normalize(request.phone());
        String email = normalizeEmail(request.email());

        validatePhoneOrEmail(phone, email);
        validatePhoneForUpdate(phone, id);
        validateEmailForUpdate(email, id);

        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(request.role());
        user.setStatus(request.status());

        user.setFullNameKm(
                requireText(
                        request.fullNameKm(),
                        "Khmer full name is required"
                )
        );

        user.setFullNameEn(
                normalize(request.fullNameEn())
        );

        user.setProfileImage(
                normalize(request.profileImage())
        );

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    /*
     * ==========================================================
     * DELETE USER
     * ==========================================================
     */

    @Override
    @Transactional
    public void deleteUser(Long id) {

        User user = findUserById(id);

        userRepository.delete(user);
    }

    /*
     * ==========================================================
     * FIND USER
     * ==========================================================
     */

    private User findUserById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        return userRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: " + id
                        )
                );
    }

    /*
     * ==========================================================
     * VALIDATE PHONE OR EMAIL
     * ==========================================================
     */

    private void validatePhoneOrEmail(
            String phone,
            String email
    ) {
        if (phone == null && email == null) {
            throw new IllegalArgumentException(
                    "At least one phone number or email is required"
            );
        }
    }

    /*
     * ==========================================================
     * CREATE VALIDATION
     * ==========================================================
     */

    private void validatePhoneForCreate(String phone) {

        if (phone == null) {
            return;
        }

        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException(
                    "Phone number is already being used"
            );
        }
    }

    private void validateEmailForCreate(String email) {

        if (email == null) {
            return;
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Email is already being used"
            );
        }
    }

    /*
     * ==========================================================
     * UPDATE VALIDATION
     * ==========================================================
     */

    private void validatePhoneForUpdate(
            String phone,
            Long userId
    ) {
        if (phone == null) {
            return;
        }

        if (userRepository.existsByPhoneAndIdNot(
                phone,
                userId
        )) {
            throw new IllegalArgumentException(
                    "Phone number is already being used"
            );
        }
    }

    private void validateEmailForUpdate(
            String email,
            Long userId
    ) {
        if (email == null) {
            return;
        }

        if (userRepository
                .existsByEmailIgnoreCaseAndIdNot(
                        email,
                        userId
                )) {
            throw new IllegalArgumentException(
                    "Email is already being used"
            );
        }
    }

    /*
     * ==========================================================
     * NORMALIZATION
     * ==========================================================
     */

    private String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeEmail(String email) {

        String normalizedEmail = normalize(email);

        if (normalizedEmail == null) {
            return null;
        }

        return normalizedEmail.toLowerCase(Locale.ROOT);
    }

    private String requireText(
            String value,
            String errorMessage
    ) {
        String normalizedValue = normalize(value);

        if (normalizedValue == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        return normalizedValue;
    }
}
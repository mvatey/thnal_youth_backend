package org.example.tnal_youth_backend.account.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.user.dto.request.CreateUserRequest;
import org.example.tnal_youth_backend.account.user.dto.request.UpdateUserRequest;
import org.example.tnal_youth_backend.account.user.dto.response.UserResponse;
import org.example.tnal_youth_backend.account.user.mapper.UserMapper;
import org.example.tnal_youth_backend.account.user.service.UserManagementService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl
        implements UserManagementService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository
                .findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user = findUser(id);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(
            CreateUserRequest request
    ) {
        String phone = normalizePhone(
                request.phone()
        );

        String email = normalizeEmail(
                request.email()
        );

        validateMemberExists(
                request.memberId()
        );

        validateCreateDuplicates(
                request.memberId(),
                phone,
                email
        );

        validateCreatedBy(
                request.createdById()
        );

        User user = User.builder()
                .memberId(request.memberId())
                .phone(phone)
                .email(email)

                /*
                 * Temporary plain-text storage for development only.
                 * Replace with BCrypt encoding when Authentication
                 * is integrated.
                 */
                .passwordHash(request.password())

                .roleId(request.roleId())
                .accountStatusId(
                        request.accountStatusId()
                )
                .failedLoginCount(0)
                .createdById(
                        request.createdById()
                )
                .build();

        try {
            User savedUser =
                    userRepository.saveAndFlush(user);

            return userMapper.toResponse(savedUser);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException(exception);
        }
    }

    @Override
    @Transactional
    public UserResponse updateUser(
            Long id,
            UpdateUserRequest request
    ) {
        User user = findUser(id);

        String phone = normalizePhone(
                request.phone()
        );

        String email = normalizeEmail(
                request.email()
        );

        validateMemberExists(
                request.memberId()
        );

        validateUpdateDuplicates(
                id,
                request.memberId(),
                phone,
                email
        );

        user.setMemberId(
                request.memberId()
        );

        user.setPhone(phone);
        user.setEmail(email);

        user.setRoleId(
                request.roleId()
        );

        user.setAccountStatusId(
                request.accountStatusId()
        );

        if (request.password() != null
                && !request.password().isBlank()) {

            /*
             * Temporary plain-text storage for development only.
             */
            user.setPasswordHash(
                    request.password()
            );
        }

        try {
            User updatedUser =
                    userRepository.saveAndFlush(user);

            return userMapper.toResponse(updatedUser);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException(exception);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {

        User user = findUser(id);

        try {
            userRepository.delete(user);
            userRepository.flush();

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    Cannot delete this user because the user is \
                    referenced by activities, participants, files, \
                    audit records, tokens, or other data.
                    """
            );
        }
    }

    private User findUser(Long id) {

        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID is required"
            );
        }

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with ID: " + id
                        )
                );
    }

    private void validateMemberExists(
            Long memberId
    ) {
        if (memberId == null) {
            return;
        }

        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member not found with ID: " + memberId
            );
        }
    }

    private void validateCreatedBy(
            Long createdById
    ) {
        if (createdById == null) {
            return;
        }

        if (!userRepository.existsById(createdById)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Creator user not found with ID: "
                            + createdById
            );
        }
    }

    private void validateCreateDuplicates(
            Long memberId,
            String phone,
            String email
    ) {
        if (userRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone is already used"
            );
        }

        if (email != null
                && userRepository
                .existsByEmailIgnoreCase(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already used"
            );
        }

        if (memberId != null
                && userRepository
                .existsByMemberId(memberId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Member already has a user account"
            );
        }
    }

    private void validateUpdateDuplicates(
            Long userId,
            Long memberId,
            String phone,
            String email
    ) {
        if (userRepository.existsByPhoneAndIdNot(
                phone,
                userId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone is already used"
            );
        }

        if (email != null
                && userRepository
                .existsByEmailIgnoreCaseAndIdNot(
                        email,
                        userId
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already used"
            );
        }

        if (memberId != null
                && userRepository
                .existsByMemberIdAndIdNot(
                        memberId,
                        userId
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Member already has a user account"
            );
        }
    }

    private String normalizePhone(String phone) {

        if (phone == null || phone.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone is required"
            );
        }

        return phone.trim();
    }

    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException
    databaseConstraintException(
            DataIntegrityViolationException exception
    ) {
        String databaseMessage =
                exception.getMostSpecificCause()
                        .getMessage();

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                User could not be saved. Check that member_id, \
                role_id, account_status_id, and created_by \
                reference existing records.

                Database message: %s
                """.formatted(databaseMessage)
        );
    }
}
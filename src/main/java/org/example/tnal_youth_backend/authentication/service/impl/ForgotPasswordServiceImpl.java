package org.example.tnal_youth_backend.authentication.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.tnal_youth_backend.authentication.model.entity.PasswordResetToken;
import org.example.tnal_youth_backend.authentication.model.entity.User;

import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;

import org.example.tnal_youth_backend.authentication.model.request.ForgotPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.request.ResetPasswordRequest;

import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;

import org.example.tnal_youth_backend.authentication.repository.PasswordResetTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;

import org.example.tnal_youth_backend.authentication.service.ForgotPasswordService;

import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;


import java.security.SecureRandom;
import java.time.OffsetDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordServiceImpl
        implements ForgotPasswordService {


    private static final int OTP_EXPIRE_MINUTES = 5;

    private static final int MAX_OTP_ATTEMPTS = 5;


    private static final SecureRandom RANDOM =
            new SecureRandom();



    private final UserRepository userRepository;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;




    // ==============================
    // REQUEST RESET PASSWORD OTP
    // ==============================

    @Override
    @Transactional
    public ApiResponse forgotPassword(
            ForgotPasswordRequest request
    ) {


        User user =
                userRepository

                        .findByPhone(
                                request.getPhoneOrEmail()
                        )

                        .or(() ->

                                userRepository.findByEmail(
                                        request.getPhoneOrEmail()
                                )

                        )

                        .orElseThrow(() ->

                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )

                        );



        String otp = generateOtp();



        PasswordResetToken token =
                PasswordResetToken.builder()


                        .user(user)


                        .otpCodeHash(
                                passwordEncoder.encode(
                                        otp
                                )
                        )


                        .deliveryChannel(
                                getChannel(
                                        request.getPhoneOrEmail()
                                )
                        )


                        .expiresAt(
                                OffsetDateTime.now()
                                        .plusMinutes(
                                                OTP_EXPIRE_MINUTES
                                        )
                        )


                        .attempts(0)


                        // FIX:
                        // database created_at NOT NULL
                        .createdAt(
                                OffsetDateTime.now()
                        )


                        .build();



        passwordResetTokenRepository.save(
                token
        );

        /*
            Development only

            Later replace with:
            - Email service
            - SMS service
        */
        log.info(
                "RESET PASSWORD OTP FOR {} : {}",
                request.getPhoneOrEmail(),
                otp
        );



        return ApiResponse.builder()

                .success(true)

                .message(
                        "OTP sent successfully"
                )

                .build();

    }

    // ==============================
    // RESET PASSWORD
    // ==============================

    @Override
    @Transactional
    public ApiResponse resetPassword(
            ResetPasswordRequest request
    ) {



        User user =
                userRepository

                        .findByPhone(
                                request.getPhoneOrEmail()
                        )


                        .or(() ->

                                userRepository.findByEmail(
                                        request.getPhoneOrEmail()
                                )

                        )


                        .orElseThrow(() ->

                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )

                        );

        PasswordResetToken token =
                passwordResetTokenRepository

                        .findTopByUserOrderByCreatedAtDesc(
                                user
                        )


                        .orElseThrow(() ->

                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "OTP not found"
                                )

                        );


        if(token.getConsumedAt() != null){


            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP already used"
            );

        }

        if(
                token.getExpiresAt()
                        .isBefore(
                                OffsetDateTime.now()
                        )
        ){


            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP expired"
            );

        }


        if(
                token.getAttempts()
                        >= MAX_OTP_ATTEMPTS
        ){


            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Too many OTP attempts"
            );

        }

        boolean otpValid =
                passwordEncoder.matches(

                        request.getOtp(),

                        token.getOtpCodeHash()

                );

        if(!otpValid){



            token.setAttempts(

                    token.getAttempts() + 1

            );



            passwordResetTokenRepository.save(
                    token
            );



            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid OTP"
            );

        }


        // update password

        user.setPasswordHash(

                passwordEncoder.encode(
                        request.getNewPassword()
                )

        );



        // unlock account after reset

        user.setStatus(
                UserStatus.ACTIVE
        );


        user.setFailedLoginCount(
                0
        );


        user.setLockedUntil(
                null
        );


        userRepository.save(
                user
        );


        // consume OTP

        token.setConsumedAt(

                OffsetDateTime.now()

        );


        passwordResetTokenRepository.save(
                token
        );



        // logout from all devices

        refreshTokenRepository.deleteByUser(
                user
        );


        return ApiResponse.builder()

                .success(true)

                .message(
                        "Password reset successfully"
                )

                .build();


    }


    // ==============================
    // GENERATE OTP
    // ==============================

    private String generateOtp(){


        int number =
                RANDOM.nextInt(
                        1_000_000
                );


        return String.format(
                "%06d",
                number
        );

    }









    // ==============================
    // DETECT OTP CHANNEL
    // ==============================

    private OtpChannel getChannel(
            String phoneOrEmail
    ){


        if(
                phoneOrEmail.contains("@")
        ){

            return OtpChannel.EMAIL;

        }


        return OtpChannel.SMS;

    }

}
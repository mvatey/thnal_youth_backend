package org.example.tnal_youth_backend.config;



import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http

                .csrf(csrf->csrf.disable())

                .cors(cors->{})

                .sessionManagement(session->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth->

                        auth

                                .requestMatchers(

                                        "/api/auth/**",

                                        "/swagger-ui/**",

                                        "/v3/api-docs/**"

                                ).permitAll()

                                .anyRequest()

                                .authenticated()

                );

        return http.build();

    }

}
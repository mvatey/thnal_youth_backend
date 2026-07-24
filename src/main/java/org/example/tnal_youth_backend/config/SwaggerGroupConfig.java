//package org.example.tnal_youth_backend.config;
//
//import org.springdoc.core.models.GroupedOpenApi;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SwaggerGroupConfig {
//
//    @Bean
//    public GroupedOpenApi memberPageApi() {
//        return GroupedOpenApi.builder()
//                .group("1. Member Page")
//                .pathsToMatch("/api/members/**")
//                .build();
//    }
//
//    @Bean
//    public GroupedOpenApi myAccountPageApi() {
//        return GroupedOpenApi.builder()
//                .group("2. My Account Page")
//                .pathsToMatch("/api/my-account/**")
//                .build();
//    }
//
////    @Bean
////    public GroupedOpenApi authenticationApi() {
////        return GroupedOpenApi.builder()
////                .group("3. Authentication")
////                .pathsToMatch(
////                        "/api/auth/**",
////                        "/api/users/me"
////                )
////                .build();
////    }
//}
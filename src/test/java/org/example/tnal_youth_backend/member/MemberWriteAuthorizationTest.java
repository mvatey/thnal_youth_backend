package org.example.tnal_youth_backend.member;

import org.example.tnal_youth_backend.member.member.controller.MemberController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberWriteAuthorizationTest {

    @Test
    void everyMemberMutationIsExplicitlyStaffOnly() {
        for (Method method : MemberController.class.getDeclaredMethods()) {
            if (!isMutation(method)) {
                continue;
            }

            PreAuthorize authorization = method.getAnnotation(PreAuthorize.class);
            assertNotNull(
                    authorization,
                    () -> method.getName() + " must declare mutation authorization"
            );

            String rule = authorization.value();
            assertTrue(rule.contains("ADMIN"), () -> method.getName() + " must allow ADMIN");
            assertTrue(rule.contains("SECRETARY"), () -> method.getName() + " must allow SECRETARY");
            assertTrue(rule.contains("BRANCH_LEADER"), () -> method.getName() + " must allow BRANCH_LEADER");
        }
    }

    private boolean isMutation(Method method) {
        return method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class);
    }
}

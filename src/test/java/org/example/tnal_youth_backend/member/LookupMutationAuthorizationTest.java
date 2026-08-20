package org.example.tnal_youth_backend.member;

import org.example.tnal_youth_backend.member.level.controller.MemberLevelController;
import org.example.tnal_youth_backend.member.religion.controller.ReligionController;
import org.example.tnal_youth_backend.member.status.controller.MemberStatusController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LookupMutationAuthorizationTest {

    @Test
    void legacyLookupMutationsAreAdminOnly() {
        List<Class<?>> controllers = List.of(
                MemberLevelController.class,
                ReligionController.class,
                MemberStatusController.class
        );

        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                if (!isMutation(method)) {
                    continue;
                }

                PreAuthorize authorization = method.getAnnotation(PreAuthorize.class);
                assertNotNull(
                        authorization,
                        () -> controller.getSimpleName() + "." + method.getName()
                                + " must declare mutation authorization"
                );
                assertEquals("hasRole('ADMIN')", authorization.value());
            }
        }
    }

    private boolean isMutation(Method method) {
        return method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
    }
}

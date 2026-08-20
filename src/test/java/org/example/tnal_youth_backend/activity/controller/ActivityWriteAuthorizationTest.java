package org.example.tnal_youth_backend.activity.controller;

import org.example.tnal_youth_backend.activity.attendance.controller.ActivityAttendanceController;
import org.example.tnal_youth_backend.activity.expense.controller.ActivityExpenseController;
import org.example.tnal_youth_backend.activity.income.controller.ActivityIncomeController;
import org.example.tnal_youth_backend.activity.media.controller.ActivityMediaController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityWriteAuthorizationTest {

    private static final List<Class<?>> ACTIVITY_CONTROLLERS = List.of(
            ActivityController.class,
            ActivityParticipantController.class,
            ActivityInvitedBranchController.class,
            ActivityExpenseController.class,
            ActivityAttendanceController.class,
            ActivityIncomeController.class,
            ActivityMediaController.class
    );

    @Test
    void everyActivityWriteEndpointIncludesAdminRole() {
        for (Class<?> controller : ACTIVITY_CONTROLLERS) {
            for (Method method : controller.getDeclaredMethods()) {
                if (!isWriteEndpoint(method)) {
                    continue;
                }

                PreAuthorize authorization = method.getAnnotation(PreAuthorize.class);

                assertThat(authorization)
                        .as("%s#%s must declare method-level write authorization",
                                controller.getSimpleName(), method.getName())
                        .isNotNull();
                assertThat(authorization.value())
                        .as("%s#%s must allow ADMIN",
                                controller.getSimpleName(), method.getName())
                        .contains("ADMIN");
                assertThat(authorization.value())
                        .as("%s#%s must keep VIEWER read-only",
                                controller.getSimpleName(), method.getName())
                        .doesNotContain("VIEWER");
            }
        }
    }

    @Test
    void activityIncomeReadsIncludeViewerRole() throws Exception {
        for (String methodName : List.of("list", "getDetail")) {
            Method method = java.util.Arrays.stream(
                            ActivityIncomeController.class.getDeclaredMethods())
                    .filter(candidate -> candidate.getName().equals(methodName))
                    .findFirst()
                    .orElseThrow();

            assertThat(method.getAnnotation(PreAuthorize.class).value())
                    .contains("VIEWER");
        }
    }

    private static boolean isWriteEndpoint(Method method) {
        return method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
    }
}

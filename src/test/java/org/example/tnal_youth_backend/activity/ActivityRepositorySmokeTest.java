package org.example.tnal_youth_backend.activity;

import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ActivityRepositorySmokeTest {
    @Autowired ActivityRepository repository;
    @Test void staffVisibilityQueryRuns() {
        repository.findStaffVisibleActivities(1L, null, null, null, false,
                null, null, PageRequest.of(0, 5));
    }
}

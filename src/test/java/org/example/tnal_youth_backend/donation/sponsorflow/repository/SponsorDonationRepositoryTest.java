package org.example.tnal_youth_backend.donation.sponsorflow.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class SponsorDonationRepositoryTest {

    @Autowired
    private SponsorDonationRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void memberLookupAcceptsNullSearch() {
        Long branchId = jdbcTemplate.queryForObject(
                "SELECT id FROM branches ORDER BY id LIMIT 1",
                Long.class
        );

        assertDoesNotThrow(() -> repository.members(branchId, null));
    }

    @Test
    void sponsorLookupAcceptsNullSearch() {
        assertDoesNotThrow(() -> repository.sponsors(null));
    }
}
